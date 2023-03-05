package top.mrxiaom.syncme.nms

import org.bukkit.Bukkit
import top.mrxiaom.syncme.nms.v1_16_R3.NMS_v1_16_R3

object NMS {
    val version = Bukkit.getServer()::class.java.`package`.name.substring(23);
    private lateinit var current: INMS
    fun init(): Boolean {
        current = when(version) {
            "v1_16_3" -> NMS_v1_16_R3()
            else -> return false
        }
        return true
    }
}