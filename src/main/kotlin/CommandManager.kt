package top.mrxiaom.syncme

import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.Permissible
import top.mrxiaom.syncme.EnumParamType.*
import top.mrxiaom.syncme.command.Command
import top.mrxiaom.syncme.command.ICommand
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

object CommandManager {
    var showHelpWhilePermDeny = false
    val parsedCommands = mutableListOf<ParsedCommand>()
    fun onCommand(
        sender: CommandSender,
        cmd: String,
        args: Array<out String>
    ) {
        val parsedParams = mutableListOf<Any>()
        parsedCommands.firstOrNull {
            it.tryParse(sender, cmd, args.joinToString(" "))?.run {
                parsedParams.addAll(this)
                true
            } ?: false
        }?.execute(sender, parsedParams) ?: run {
            sender.sendMessage(
                generateHelpMessages(sender)
                    .joinToString("\n")
            )
        }
    }

    fun onTabComplete(
        sender: CommandSender,
        cmd: String,
        args: Array<out String>
    ): MutableList<String> {
        val tab = mutableListOf<String>()
        return tab
    }

    val regexParam = Regex("\\{.*?}\\+?")
    inline fun <reified T : ICommand> register(cmd: T) {
        var count = 0
        T::class.declaredMemberFunctions.forEach { func ->
            // 过滤掉没有 @Command 注解的函数
            val meta = func.findAnnotation<Command>() ?: return@forEach
            // 生成参数列表以及正则表达式
            val params = mutableListOf<String>()
            val regex = Regex("^" + (regexParam.split(meta.args) { s, isMatched ->
                if (isMatched) (if (s.endsWith("+")) "(.*)" else "([^ ]+)").also {
                    params.add(s.removeSuffix("+").removeSurrounding("{", "}"))
                } else s
            }?.joinToString("") ?: meta.args))
            // 确定第一个参数是否为发送者，以及是否仅有玩家可用该命令
            val playerOnly = func.parameters.first().type.jvmErasure.also {
                if (it.isSubclassOf(CommandSender::class)) return@forEach
            } == Player::class
            // 确定参数列表与函数参数的对应关系
            val paramIndexMap = mutableMapOf<Int, Int>()
            // 确定参数类型
            val paramType = mutableMapOf<Int, EnumParamType>()
            for (i in func.parameters.indices) {
                if (i <= 1) continue
                val param = func.parameters[i]
                val oldIndex = params.indexOf(param.name)
                if (oldIndex < 0) return@forEach
                paramIndexMap[oldIndex] = i - 2
                paramType[oldIndex] = when (param.type.jvmErasure) {
                    String::class -> STRING
                    OfflinePlayer::class -> PLAYER
                    Player::class -> PLAYER_ONLINE
                    Int::class -> INT
                    Double::class -> DOUBLE
                    Boolean::class -> BOOLEAN
                    else -> NULL
                }
            }
            parsedCommands.add(ParsedCommand(cmd, meta, regex, playerOnly, paramIndexMap, paramType, func))
            count++
        }
        println("[CommandManager] $count commands from ${T::class.simpleName} have been registered.")
    }

    fun generateHelpMessages(p: Permissible): List<String> =
        Lang.COMMAND__HELP.list(
            SyncMe.description.version,
            SyncMe.description.authors.joinToString()
        ) + parsedCommands.toSet()
            .filter { it.meta.help != Lang.PREFIX }
            .filter { p.hasPermission(it.meta.permission) }
            .map { it.meta.help.get() } + parsedCommands
            .filter { it.meta.help == Lang.PREFIX }
            .filter { p.hasPermission(it.meta.permission) }
            .map { it.meta.helpExtra }
            .filterNot { it.isBlank() }
            .toSet()
}

enum class EnumParamType {
    STRING, PLAYER, PLAYER_ONLINE, INT, DOUBLE, BOOLEAN, NULL
}

class ParsedCommand(
    val instance: ICommand,
    val meta: Command,
    val regex: Regex,
    val playerOnly: Boolean,
    private val paramIndexMap: Map<Int, Int>,
    private val paramTypes: Map<Int, EnumParamType>,
    private val func: KFunction<*>
) {
    fun tryParse(sender: CommandSender, cmd: String, params: String): List<Any>? {
        if (playerOnly && sender !is Player) return null
        if (meta.cmd.isNotEmpty() && !meta.cmd.contains(cmd.lowercase())) return null
        val match = regex.find(params) ?: return null
        if (meta.permission.isNotEmpty() && !sender.hasPermission(meta.permission))
            return listOf(STRING)
        val paramsList = mutableListOf<Any>()
        var i = 0
        for (s in match.groupValues.drop(1)) {
            paramsList.add(when (paramTypes[i]) {
                STRING -> s
                PLAYER -> Bukkit.getOfflinePlayers().firstOrNull { s == (it.name ?: NULL) } ?: PLAYER
                PLAYER_ONLINE -> Bukkit.getOnlinePlayers().firstOrNull { s == it.name } ?: PLAYER_ONLINE
                INT -> s.toIntOrNull() ?: INT
                DOUBLE -> s.toDoubleOrNull() ?: DOUBLE
                BOOLEAN -> s.toBooleanOrNull() ?: BOOLEAN
                else -> NULL
            }.also { i++ })
        }

        val finalParams = Array<Any>(paramIndexMap.size) { }
        paramIndexMap.forEach { oldIndex, funcIndex ->
            finalParams[funcIndex] = paramsList[oldIndex]
        }
        return finalParams.toList()
    }

    fun execute(sender: CommandSender, params: List<Any>) {
        params.filterIsInstance<EnumParamType>().firstOrNull()?.apply {
            sender.sendMessage(
                when (this) {
                    STRING -> if (CommandManager.showHelpWhilePermDeny) null
                    else Lang.COMMAND__ERROR__NO_PERMISSION

                    NULL -> null
                    PLAYER -> Lang.COMMAND__ERROR__NO_PLAYER
                    PLAYER_ONLINE -> Lang.COMMAND__ERROR__PLAYER_NOT_ONLINE
                    INT -> Lang.COMMAND__ERROR__NOT_INTEGER
                    DOUBLE -> Lang.COMMAND__ERROR__NOT_DOUBLE
                    BOOLEAN -> Lang.COMMAND__ERROR__NOT_BOOLEAN
                }?.get() ?: CommandManager.generateHelpMessages(sender).joinToString("\n")
            )
            return
        }
        if (func.isSuspend) SyncMe.launch {
            func.callSuspend(instance, sender, *params.toTypedArray())
        } else {
            func.call(instance, sender, *params.toTypedArray())
        }
    }
}

fun <T> Regex.split(
    input: CharSequence,
    transform: (s: String, isMatched: Boolean) -> T?
): List<T>? {
    if (!containsMatchIn(input)) return null
    val list = mutableListOf<T>()
    var index = 0
    for (result in findAll(input)) {
        val first = result.range.first
        val last = result.range.last
        if (first > index) {
            val value = transform(input.substring(index, first), false)
            if (value != null) list.add(value)
        }
        val value = transform(input.substring(first, last + 1), true)
        if (value != null) list.add(value)
        index = last + 1
    }
    if (index < input.length) {
        val value = transform(input.substring(index), false)
        if (value != null) list.add(value)
    }
    return list
}

fun String.toBooleanOrNull(): Boolean? = when (lowercase()) {
    "true" -> true
    "t" -> true
    "yes" -> true
    "y" -> true
    "false" -> false
    "f" -> false
    "no" -> false
    "n" -> false
    else -> null
}
