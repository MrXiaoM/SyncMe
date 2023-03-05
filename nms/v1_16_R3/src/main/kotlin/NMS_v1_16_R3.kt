package top.mrxiaom.syncme.nms.v1_16_R3

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.internal.Streams
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.mojang.datafixers.DataFixer
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Bukkit
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
        object : TypeToken<Map<MinecraftKey, AdvancementProgress>>() { }
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
}