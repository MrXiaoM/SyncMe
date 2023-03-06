package top.mrxiaom.syncme.nms

import org.bukkit.Bukkit
import top.mrxiaom.syncme.nms.v1_16_R3.NMS_v1_16_R3

object NMS {
    val version = Bukkit.getServer()::class.java.`package`.name.substring(23);
    private lateinit var _current: INMS
    val current
        get() = _current
    fun init(): Boolean {
        _current = when(version) {
            "v1_16_3" -> NMS_v1_16_R3()
            else -> return false
        }
        return true
    }
}