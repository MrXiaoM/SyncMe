package top.mrxiaom.syncme.nms.v1_16_R3

import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import top.mrxiaom.syncme.nms.INMS

class NMS_v1_16_R3 : INMS {
    override fun getAdvancements(p: Player): String {
        val player = p as CraftPlayer
        player.handle.advancementData.data

        TODO("Not yet implemented")
    }

    override fun setAdvancements(p: Player, s: String) {
        val player = p as CraftPlayer
        player.handle.advancementData.data

        TODO("Not yet implemented")
    }
}