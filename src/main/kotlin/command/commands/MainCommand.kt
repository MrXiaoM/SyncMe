package top.mrxiaom.syncme.command.commands

import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.mrxiaom.syncme.Lang
import top.mrxiaom.syncme.SyncMe
import top.mrxiaom.syncme.command.Command
import top.mrxiaom.syncme.command.ICommand
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
        args = "push {target} {type}",
        help = Lang.COMMAND__PUSH__HELP,
        permission = "$perm.command.push"
    )
    fun syncPush(sender: CommandSender, target: OfflinePlayer, type: String) {

    }

    @Command(
        args = "push me {type}",
        help = Lang.COMMAND__PUSH__HELP,
        permission = "$perm.command.push"
    )
    fun syncPushSelf(sender: Player, type: String) {

    }

    @Command(
        args = "push all {type}",
        help = Lang.COMMAND__PUSH__HELP,
        permission = "$perm.command.push"
    )
    fun syncPushAll(sender: CommandSender, type: String) {

    }

    @Command(
        args = "pull {target} {type}",
        help = Lang.COMMAND__PULL__HELP,
        permission = "$perm.command.pull"
    )
    fun syncPull(sender: CommandSender, target: OfflinePlayer, type: String) {

    }

    @Command(
        args = "pull me {type}",
        help = Lang.COMMAND__PULL__HELP,
        permission = "$perm.command.pull"
    )
    fun syncPullSelf(sender: Player, type: String) {

    }

    @Command(
        args = "pull all {type}",
        help = Lang.COMMAND__PULL__HELP,
        permission = "$perm.command.pull"
    )
    fun syncPullAll(sender: CommandSender, type: String) {

    }
}