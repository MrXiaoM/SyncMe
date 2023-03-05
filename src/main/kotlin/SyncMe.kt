package top.mrxiaom.syncme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import top.mrxiaom.sqlhelper.SQLHelper
import top.mrxiaom.sqlhelper.SQLang
import top.mrxiaom.syncme.command.commands.MainCommand
import top.mrxiaom.syncme.command.register
import top.mrxiaom.syncme.listener.PlayerListener
import top.mrxiaom.syncme.nms.NMS
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

const val perm = "top.mrxiaom.syncme"

object SyncMe : JavaPlugin(), CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    var reconnect = false
    lateinit var sql: SQLHelper
    lateinit var langFile: File
    override fun onLoad() {
        langFile = File(dataFolder, "lang.yml")
        Lang.load(langFile)

        reloadConfig()
    }

    override fun onEnable() {
        if (!NMS.init()) {
            logger.warning("当前版本 ${NMS.version} 不受支持!")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        MainCommand.register()

        PlayerListener.register()
    }

    override fun onDisable() {
        cancel("Plugin disabled.")
        closeSQLConnection()
    }

    fun reloadConfig(reco: Boolean) {
        reconnect = reco
        reloadConfig()
    }

    override fun reloadConfig() {
        saveDefaultConfig()
        super.reloadConfig()
        if (!sqlConnected || reconnect) {
            closeSQLConnection()
            SQLHelper.connectToMySQL(
                config.getString("mysql.host"),
                config.getInt("mysql.port"),
                config.getString("mysql.user"),
                config.getString("mysql.pass"),
                config.getString("mysql.database")
            ).ifPresent {
                reconnect = false
                sql = it.apply {
                    val prefix = config.getString("mysql.table-prefix")
                    SQLang.createTable(connection, "${prefix}inventory")
                }
            }
        }

    }

    val sqlConnected: Boolean
        get() = this::sql.isInitialized && sql.connection?.isClosed == false

    fun closeSQLConnection() {
        if (sqlConnected) sql.close()
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean = CommandManager.onCommand(sender, label, args).let { true }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> = CommandManager.onTabComplete(sender, command.name, args)
}
