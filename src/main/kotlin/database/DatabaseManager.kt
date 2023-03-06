package top.mrxiaom.syncme.database

import kotlinx.coroutines.async
import org.bukkit.OfflinePlayer
import top.mrxiaom.syncme.SyncMe
import top.mrxiaom.syncme.database.DatabaseManager.Type.*
import top.mrxiaom.syncme.nms.NMS
import java.sql.Connection

object DatabaseManager {
    internal var prefix = ""
    internal var server = "default"
    internal lateinit var connection: Connection

    enum class TablePlayerData(
        val type: String = "longtext"
    ) {
        ADVANCEMENTS,
        INVENTORY,
        ENDERCHEST,
        POTIONS,
        LEVEL("text(64)"),
        STATISTIC;

        companion object {
            val table
                get() = "${prefix}player_dat"

            fun check(conn: Connection) {
                val s = conn.createStatement()
                s.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS $table 
                        (`name` VARCHAR(64) NOT NULL, ${values().joinToString { "`${it.name.lowercase()}` ${it.type}" }}, PRIMARY KEY (`name`));
                    """.trimIndent()
                )
                s.close()
            }
        }
    }

    fun init(conn: Connection) {
        connection = conn
        TablePlayerData.check(conn)
    }

    enum class Type {
        ADVANCEMENTS,
        INVENTORY,
        ENDERCHEST,
        POTIONS,
        LEVEL,
        STATISTIC;

        companion object {
            val ALL
                get() = values()
            val ESSENTIAL
                get() = arrayOf(INVENTORY, ENDERCHEST, POTIONS, LEVEL)

            fun parse(s: String): Array<Type> = when (s.lowercase()) {
                "all" -> ALL
                "essential" -> ESSENTIAL
                else -> values().filter { it.name.lowercase() == s }.toTypedArray()
            }
        }
    }

    suspend fun push(p: OfflinePlayer, vararg types: Type): Boolean = SyncMe.async {
        val player = NMS.current.loadPlayer(p) ?: return@async false
        val values = NMS.current.run {
            types.map {
                when (it) {
                    ADVANCEMENTS -> getAdvancements(player)
                    INVENTORY -> getInventory(player)
                    ENDERCHEST -> getEnderChest(player)
                    POTIONS -> getPotions(player)
                    LEVEL -> player.exp.toString()
                    STATISTIC -> ""
                }
            }
        }
        val insertColumns = types.joinToString { "`${it.name.lowercase()}`" }
        val insertValues = types.joinToString { "?" }
        val updateValues = types.joinToString { "`${it.name.lowercase()}` = ?" }
        val sql = """
            INSERT INTO ${TablePlayerData.table} (`name`, $insertColumns) 
            VALUES (?, $insertValues) 
            ON DUPLICATE KEY UPDATE $updateValues
            """.trimIndent()
        val ps = connection.prepareStatement(sql)
        ps.setObject(0, player.name.lowercase())
        for (i in values.indices) {
            ps.setObject(i + 1, values[i])
            ps.setObject(i + 1 + values.size, values[i])
        }
        ps.executeUpdate()
        ps.close()
        return@async true
    }.await()

    suspend fun pull(p: OfflinePlayer, vararg types: Type): Boolean = SyncMe.async {
        val player = NMS.current.loadPlayer(p) ?: return@async false
        val selectColumns = types.joinToString { "`${it.name.lowercase()}`" }
        val sql = """
            SELECT $selectColumns FROM ${TablePlayerData.table} 
            WHERE `name` = ?
        """.trimIndent()
        val ps = connection.prepareStatement(sql)
        ps.setObject(0, p)
        val result = ps.executeQuery()
        if (!result.next()) {
            result.close()
            ps.close()
            return@async false
        }
        NMS.current.apply {
            for (type in types) {
                val s = result.getObject(type.name.lowercase()).toString()
                when (type) {
                    ADVANCEMENTS -> setAdvancements(player, s)
                    INVENTORY -> setInventory(player, s)
                    ENDERCHEST -> setEnderChest(player, s)
                    POTIONS -> setPotions(player, s)
                    LEVEL -> player.exp = s.toFloat()
                    STATISTIC -> Unit
                }
            }
            savePlayer(player)
        }
        return@async true
    }.await()
}
