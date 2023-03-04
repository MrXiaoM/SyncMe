import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin
import top.mrxiaom.syncme.CommandManager
import top.mrxiaom.syncme.command.commands.MainCommand
import top.mrxiaom.syncme.command.register
import top.mrxiaom.syncme.split
import top.mrxiaom.syncme.toBooleanOrNull
import java.util.*

fun main() {
    MainCommand.register()
    while (true) {
        val input = readln()
        if (input == "stop") break
        dispatchCommand(input)
    }
}

fun dispatchCommand(s: String) {
    val split = s.split(" ")
    CommandManager.onCommand(DummyCommandSender, split[0], split.drop(1).toTypedArray())
}

object DummyCommandSender: CommandSender{
        override fun isOp(): Boolean {
            return true
        }

        override fun setOp(value: Boolean) {

        }

        override fun isPermissionSet(name: String): Boolean {
            return true
        }

        override fun isPermissionSet(perm: Permission): Boolean {
            return true
        }

        override fun hasPermission(name: String): Boolean {
            return true
        }

        override fun hasPermission(perm: Permission): Boolean {
            return true
        }

        override fun addAttachment(plugin: Plugin, name: String, value: Boolean): PermissionAttachment {
            TODO("Not yet implemented")
        }

        override fun addAttachment(plugin: Plugin): PermissionAttachment {
            TODO("Not yet implemented")
        }

        override fun addAttachment(plugin: Plugin, name: String, value: Boolean, ticks: Int): PermissionAttachment? {
            TODO("Not yet implemented")
        }

        override fun addAttachment(plugin: Plugin, ticks: Int): PermissionAttachment? {
            TODO("Not yet implemented")
        }

        override fun removeAttachment(attachment: PermissionAttachment) {
            TODO("Not yet implemented")
        }

        override fun recalculatePermissions() {
            TODO("Not yet implemented")
        }

        override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
            TODO("Not yet implemented")
        }

        override fun sendMessage(message: String) {
            println(message)
        }

        override fun sendMessage(vararg messages: String?) {
            println(messages)
        }

        override fun sendMessage(sender: UUID?, message: String) {
            println(message)
        }

        override fun sendMessage(sender: UUID?, vararg messages: String?) {
            println(messages)
        }

        override fun getServer(): Server {
            TODO("Not yet implemented")
        }

        override fun getName(): String {
            return "console"
        }

        override fun spigot(): CommandSender.Spigot {
            TODO("Not yet implemented")
        }

}