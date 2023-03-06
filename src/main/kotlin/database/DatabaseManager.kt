package top.mrxiaom.syncme.database

import top.mrxiaom.syncme.SyncMe
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
}
