package top.mrxiaom.syncme.nms

import org.bukkit.entity.Player

interface INMS {
    /**
     * 导出玩家的成就/进度数据
     */
    fun Player.adancement(): String

    /**
     * 导入玩家的
     */
    fun Player.adancement(s: String)
}