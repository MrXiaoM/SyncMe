package top.mrxiaom.syncme

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.PluginDescriptionFile
import java.io.File

enum class Lang(
    private val s: String,
    private val hasPrefix: Boolean = true
) {
    PREFIX("&8[&3&lSync&b&lMe&8]&f ", false),
    COMMAND__HELP(
        listOf(
            "&7&m --------[&r &3&lSyncMe &7&m]------- &r",
            "&6>> &2正在运行 &3SyncMe &b$0 &2by &e$1"
        )
    ),
    COMMAND__PUSH__HELP(
        "/sync push &e<玩家/me/*> &7[类型] &8- &b手动推送本地所有(默认)或指定类型数据到数据库"
    ),
    COMMAND__PULL__HELP(
        "/sync pull &e<玩家/me/*> &7[类型] &8- &b从数据库手动拉取所有(默认)或指定类型数据到本地"
    ),
    COMMAND__CONNECT__HELP(
        "/sync connect &8- &b重载配置文件 并重新连接数据库"
    ),
    COMMAND__RELOAD__HELP(
        "/sync reload &8- &b仅重载配置文件"
    ),
    COMMAND__ERROR__NO_PERMISSION(
        "&e你没有执行该命令的权限"
    ),
    COMMAND__ERROR__NOT_INTEGER(
        "&e你需要输入一个整数"
    ),
    COMMAND__ERROR__NOT_DOUBLE(
        "&e你需要输入一个数字"
    ),
    COMMAND__ERROR__NOT_BOOLEAN(
        "&e你需要输入一个布尔值 &7(是:y, 否:n"
    ),
    COMMAND__ERROR__PLAYER_NOT_ONLINE(
        "&e该玩家不在线"
    ),
    COMMAND__ERROR__NO_PLAYER(
        "&e找不到该玩家"
    ),
    COMMAND__ERROR__NO_TYPE(
        "&e找不到这种数据类型"
    );

    constructor(
        s: List<String>,
        hasPrefix: Boolean = true
    ) : this(s.joinToString("\n"), hasPrefix)

    private val defaultText: String
        get() = if (!hasPrefix) s
        else if (s.contains("\n")) s
            .split("\n")
            .joinToString("\n") { PREFIX.s + s }
        else PREFIX.s + s
    private val configKey = name.replace("__", ".").replace("_", "-")
    fun get(vararg args: String): String {
        if (s.contains("\n"))
            return list(*args).joinToString("\n")
        return colored.getOrElse(this) {
            yaml.set(configKey, s)
            defaultText.color()
        }.replaceArguments(args)
    }

    fun list(vararg args: String): List<String> {
        return coloredList.getOrElse(this) {
            colored[this]?.also {
                return listOf(it)
            }

            yaml.set(configKey, s.split("\n"))
            defaultText.color().split("\n").also {
                coloredList[this] = it
            }
        }.map { it.replaceArguments(args) }
    }

    private fun String.replaceArguments(args: Array<out String>): String {
        var str = this
        for (i in args.indices) {
            str = replace("\$$i", args[i])
        }
        return str
    }

    companion object {
        private val desc: PluginDescriptionFile
            get() = SyncMe.description
        private val yaml = YamlConfiguration()
        private val colored = mutableMapOf<Lang, String>()
        private val coloredList = mutableMapOf<Lang, List<String>>()

        fun load(file: File) {
            colored.clear()
            coloredList.clear()
            if (file.exists()) yaml.load(file)
            for (l in Lang.values()) {
                val key = l.configKey
                if (!yaml.contains(key)) {
                    yaml.set(
                        key,
                        if (!l.s.contains("\n")) l.s
                        else l.s.split("\n")
                    )
                }
                yaml.getString(key, null)?.apply {
                    colored[l] = color()
                } ?: yaml.getStringList(key).apply {
                    coloredList[l] = joinToString("\n").color().split("\n")
                }
            }
            save(file)
        }

        fun save(file: File) = yaml.save(file)
    }
}