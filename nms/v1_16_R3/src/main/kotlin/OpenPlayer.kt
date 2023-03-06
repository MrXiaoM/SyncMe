package top.mrxiaom.syncme.nms.v1_16_R3

import net.minecraft.server.v1_16_R3.EntityPlayer
import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools
import net.minecraft.server.v1_16_R3.NBTTagCompound
import org.apache.logging.log4j.LogManager
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import java.io.File
import java.io.FileOutputStream

/**
 * https://github.com/lishid/OpenInv/blob/1c579564bc39da0b6c6bd52d1bc164d7b4d9b2d9/internal/v1_16_R3/src/main/java/com/lishid/openinv/internal/v1_16_R3/OpenPlayer.java
 */
class OpenPlayer(
    server: CraftServer,
    entity: EntityPlayer
) : CraftPlayer(server, entity) {
    override fun saveData() {
        super.saveData()
        val player = this.handle
        // See net.minecraft.server.WorldNBTStorage#save(EntityPlayer)
        try {
            val worldNBTStorage = player.server.playerList.playerFileData
            val playerData = player.save(NBTTagCompound())
            if (!isOnline) {
                // Special case: save old vehicle data
                val oldData = worldNBTStorage.load(player)
                if (oldData != null && oldData.hasKeyOfType("RootVehicle", 10)) {
                    // See net.minecraft.server.PlayerList#a(NetworkManager, EntityPlayer) and net.minecraft.server.EntityPlayer#b(NBTTagCompound)
                    playerData["RootVehicle"] = oldData.getCompound("RootVehicle")
                }
            }
            val file = File(worldNBTStorage.playerDir, player.uniqueIDString + ".dat.tmp")
            val file1 = File(worldNBTStorage.playerDir, player.uniqueIDString + ".dat")
            NBTCompressedStreamTools.a(playerData, FileOutputStream(file))
            if (file1.exists() && !file1.delete() || !file.renameTo(file1)) {
                LogManager.getLogger().warn("Failed to save player data for {}", player.getDisplayName().string)
            }
        } catch (e: Exception) {
            LogManager.getLogger().warn("Failed to save player data for {}", player.getDisplayName().string)
        }
    }
}
