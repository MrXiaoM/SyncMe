package top.mrxiaom.syncme.command

import org.bukkit.command.CommandSender
import top.mrxiaom.syncme.CommandManager
import top.mrxiaom.syncme.Lang

interface ICommand
interface ITabCompleter {
    fun tabComplete(
        sender: CommandSender,
        cmd: String,
        args: List<String>
    ): List<String>? = null
}

inline fun <reified T : ICommand> T.register() {
    CommandManager.register(this)
}

@Target(AnnotationTarget.FUNCTION)
annotation class Command(
    val cmd: Array<String> = [],
    val args: String,
    val permission: String = "",
    val help: Lang = Lang.PREFIX,
    val helpExtra: String = ""
)