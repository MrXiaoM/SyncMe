package top.mrxiaom.syncme.nms

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

interface INMS {
    /**
     * 获取玩家的成就/进度数据
     */
    fun getAdvancements(p: Player): String

    /**
     * 获取玩家的成就/进度数据
     */
    fun setAdvancements(p: Player, s: String)

    /**
     * 获取玩家的物品栏
     */
    fun getInventory(p: Player): String {
        val output = ByteArrayOutputStream()
        val stream = BukkitObjectOutputStream(output)
        stream.writeInt(p.inventory.contents.size)
        p.inventory.contents.forEach {
            stream.writeObject(it)
        }
        stream.close()
        return Base64Coder.encodeLines(output.toByteArray())
    }

    /**
     * 设置玩家的物品栏
     */
    fun setInventory(p: Player, s: String) {
        val input = ByteArrayInputStream(Base64Coder.decodeLines(s))
        val stream = BukkitObjectInputStream(input)
        val items = arrayOfNulls<ItemStack>(Math.min(p.inventory.contents.size, stream.readInt()))
        for (i in items.indices) {
            items[i] = stream.readObject() as ItemStack
        }
        stream.close()
        p.inventory.contents = items
    }

    /**
     * 获取玩家的末影箱
     */
    fun getEnderChest(p: Player): String {
        val output = ByteArrayOutputStream()
        val stream = BukkitObjectOutputStream(output)
        stream.writeInt(p.enderChest.size)
        p.enderChest.contents.forEach {
            stream.writeObject(it)
        }
        stream.close()
        return Base64Coder.encodeLines(output.toByteArray())
    }

    /**
     * 设置玩家的末影箱
     */
    fun setEnderChest(p: Player, s: String) {
        val input = ByteArrayInputStream(Base64Coder.decodeLines(s))
        val stream = BukkitObjectInputStream(input)
        val items = arrayOfNulls<ItemStack>(Math.min(p.enderChest.contents.size, stream.readInt()))
        for (i in items.indices) {
            items[i] = stream.readObject() as ItemStack
        }
        stream.close()
        p.enderChest.contents = items
    }

    /**
     * 获取玩家的药水效果
     */
    fun getPotions(p: Player): String {
        val list = mutableListOf<String>()
        for (effect in PotionEffectType.values()) {
            p.getPotionEffect(effect)?.apply {
                list.add("$type:$duration:$amplifier:$isAmbient")
            }
        }
        return list.joinToString(",")
    }

    /**
     * 设置玩家的药水效果
     */
    fun setPotions(p: Player, s: String) {
        for (effect in s.split(",")) {
            val args = effect.split(":")
            if (args.size != 4) continue
            val type = PotionEffectType.getByName(args[0]) ?: continue
            val duration = args[1].toIntOrNull() ?: continue
            val amplifier = args[2].toIntOrNull() ?: continue
            val isAmbient = args[3].toBooleanStrictOrNull() ?: continue
            if (p.hasPotionEffect(type)) p.removePotionEffect(type)
            p.addPotionEffect(PotionEffect(type, duration, amplifier, isAmbient))
        }
    }

    /**
     * 从离线玩家加载玩家实例
     *
     * Thanks [OpenInv](https://github.com/lishid/OpenInv/blob/1c579564bc39da0b6c6bd52d1bc164d7b4d9b2d9/internal/v1_16_R3/src/main/java/com/lishid/openinv/internal/v1_16_R3/PlayerDataManager.java#L81-L116) for the amazing solution.
     */
    fun loadPlayer(p: OfflinePlayer): Player?
    fun savePlayer(p: Player)
}
