package top.mrxiaom.syncme.nms.v1_16_R3

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.internal.Streams
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.mojang.authlib.GameProfile
import com.mojang.datafixers.DataFixer
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import top.mrxiaom.syncme.nms.INMS
import java.io.StringReader

class NMS_v1_16_R3 : INMS {
    val gson4Advancement = GsonBuilder()
        .registerTypeAdapter(AdvancementProgress::class.java, AdvancementProgress.a())
        .registerTypeAdapter(MinecraftKey::class.java, MinecraftKey.a())
        .create()
    val gson4AdvTypeToken: TypeToken<Map<MinecraftKey, AdvancementProgress>> =
        object : TypeToken<Map<MinecraftKey, AdvancementProgress>>() {}
    private val bukkitEntity = kotlin.runCatching {
        Entity::class.java.getDeclaredField("bukkitEntity").also { it.isAccessible = true }
    }.onFailure {
        IllegalStateException("[SyncMe] Unable to obtain field to inject custom save process", it).printStackTrace()
    }.getOrNull()

    override fun getAdvancements(p: Player): String {
        val player = p as CraftPlayer
        val data = player.handle.advancementData.data
        val map = hashMapOf<MinecraftKey, AdvancementProgress>()

        for ((key, progress) in data.entries) {
            if (progress.b()) {
                map[key.name] = progress
            }
        }
        val json = gson4Advancement.toJsonTree(map)
        json.asJsonObject.addProperty("DataVersion", SharedConstants.getGameVersion().worldVersion)
        return gson4Advancement.toJson(json)
    }

    private val AdvancementDataPlayer._d: DataFixer
        get() = AdvancementDataPlayer::class.java.getDeclaredField("d")
            .also { it.isAccessible = true }.get(this) as DataFixer

    override fun setAdvancements(p: Player, s: String) {
        val player = p as CraftPlayer
        val advancementData = (Bukkit.getServer() as CraftServer).server.advancementData
        val playerData = player.handle.advancementData

        val reader = JsonReader(StringReader(s)).also { it.isLenient = false }
        var dynamic = Dynamic(JsonOps.INSTANCE, Streams.parse(reader))
        if (!dynamic["DataVersion"].asNumber().result().isPresent) {
            dynamic = dynamic.set("DataVersion", dynamic.createInt(1343))
        }
        dynamic = playerData._d.update(
            DataFixTypes.ADVANCEMENTS.a(), dynamic,
            dynamic["DataVersion"].asInt(0),
            SharedConstants.getGameVersion().worldVersion
        )
        dynamic = dynamic.remove("DataVersion")

        val map = gson4Advancement.getAdapter(gson4AdvTypeToken)
            .fromJsonTree(dynamic.value)
            ?: throw JsonParseException("Found null for advancements")

        playerData.data.clear()
        for ((key, progress) in map.entries) {
            val advancement = advancementData.a(key) ?: continue

            progress.a(advancement.criteria, advancement.i())
            playerData.data[advancement] = progress
        }
    }

    override fun loadPlayer(p: OfflinePlayer): Player? {
        // Ensure player has data
        if (!p.hasPlayedBefore()) return null

        // No need to load if player is online
        if (p is Player) return p
        if (p.isOnline) return p.player

        // Create a profile and entity to load the player data
        // See net.minecraft.server.PlayerList#attemptLogin
        val profile = GameProfile(
            p.uniqueId,
            if (p.name != null) p.name else p.uniqueId.toString()
        )
        val server: MinecraftServer = (Bukkit.getServer() as CraftServer).server
        val worldServer = server.getWorldServer(World.OVERWORLD) ?: return null

        val entity = EntityPlayer(server, worldServer, profile, PlayerInteractManager(worldServer))

        try {
            // inject for saving data
            // injectPlayer(entity)
            bukkitEntity?.set(entity, OpenPlayer(entity.server.server, entity))
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        // Get and return the bukkit entity
        return entity.bukkitEntity?.also { it.loadData() }
    }

    override fun savePlayer(p: Player) = (p as CraftPlayer).saveData()
}