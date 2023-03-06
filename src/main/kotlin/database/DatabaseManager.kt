package top.mrxiaom.syncme.database

import top.mrxiaom.syncme.SyncMe
import java.sql.Connection

object DatabaseManager {
    internal var prefix = ""
    internal var server = "default"
    internal lateinit var connection: Connection

    fun init(conn: Connection) {
        connection = conn
    }
}
