package top.mrxiaom.syncme.command.commands

import kotlinx.coroutines.launch
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.mrxiaom.syncme.Config
import top.mrxiaom.syncme.Lang
import top.mrxiaom.syncme.SyncMe
import top.mrxiaom.syncme.command.Command
import top.mrxiaom.syncme.command.ICommand
import top.mrxiaom.syncme.database.DatabaseManager
import top.mrxiaom.syncme.perm

object MainCommand : ICommand {

    @Command(
        args = "reload",
        help = Lang.COMMAND__RELOAD__HELP,
        permission = "$perm.command.reload"
    )
    fun reload(sender: CommandSender) {
        SyncMe.reloadConfig(false)
    }

    @Command(
        args = "connect",
        help = Lang.COMMAND__CONNECT__HELP,
        permission = "$perm.command.connect"
    )
    fun connect(sender: CommandSender) {
        SyncMe.reloadConfig(true)
    }

    @Command(
        args = "push {target} {type}+",
        help = Lang.COMMAND__PUSH__HELP,
        permission = "$perm.command.push"
    )
    suspend fun syncPush(sender: CommandSender, target: OfflinePlayer, type: String?) {
        val types = DatabaseManager.Type.parse(type ?: Config.fallback_type.get())
        if (types.isEmpty()) {
            sender.sendMessage(Lang.COMMAND__ERROR__NO_TYPE.get())
            return
        }
        DatabaseManager.push(target, *types)
    }

    @Command(
        args = "push me {type}+",
        help = Lang.COMMAND__PUSH__HELP,
        permission = "$perm.command.push"
    )
    suspend fun syncPushSelf(sender: Player, type: String?) {
        val types = DatabaseManager.Type.parse(type ?: Config.fallback_type.get())
        if (types.isEmpty()) {
            sender.sendMessage(Lang.COMMAND__ERROR__NO_TYPE.get())
            return
        }
        DatabaseManager.push(sender, *types)
    }

    @Command(
        args = "pull {target} {type}+",
        help = Lang.COMMAND__PULL__HELP,
        permission = "$perm.command.pull"
    )
    suspend fun syncPull(sender: CommandSender, target: OfflinePlayer, type: String?) {
        val types = DatabaseManager.Type.parse(type ?: Config.fallback_type.get())
        if (types.isEmpty()) {
            sender.sendMessage(Lang.COMMAND__ERROR__NO_TYPE.get())
            return
        }
        DatabaseManager.pull(target, *types)
    }

    @Command(
        args = "pull me {type}+",
        help = Lang.COMMAND__PULL__HELP,
        permission = "$perm.command.pull.self"
    )
    suspend fun syncPullSelf(sender: Player, type: String?) {
        val types = DatabaseManager.Type.parse(type ?: Config.fallback_type.get())
        if (types.isEmpty()) {
            sender.sendMessage(Lang.COMMAND__ERROR__NO_TYPE.get())
            return
        }
        DatabaseManager.pull(sender, *types)
    }
}