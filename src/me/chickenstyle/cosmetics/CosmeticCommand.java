package me.chickenstyle.cosmetics;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.chickenstyle.cosmetics.utilities.Utils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineskin.data.Skin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

public class CosmeticCommand implements CommandExecutor {
    private BetterCosmetics main;
    private Skin skin;


    public CosmeticCommand(BetterCosmetics main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {

        if (args.length != 1 || !(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (new File(main.getDataFolder() + "/CustomSkins/" + args[0]).exists()) {
            player.sendMessage(Utils.color("&7Applying Cosmetic..."));


            try {



                File newSkin = Utils.applyCosmetic(player.getUniqueId(),new File(main.getDataFolder() + "/CustomSkins/" + args[0]));
                main.getSkinClient().generateUpload(newSkin).thenAccept(skin -> {
                    this.skin = skin;
                    System.out.println(skin.data.texture.url);
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }



            new BukkitRunnable() {

                @Override
                public void run() {
                    GameProfile profile = ((CraftPlayer)player).getHandle().getProfile();
                    PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;

                    connection.sendPacket(new PacketPlayOutEntityDestroy(player.getEntityId()));
                    connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,((CraftPlayer)player).getHandle()));

                    profile.getProperties().removeAll("textures");

                    profile.getProperties().put("textures",new Property("textures",skin.data.texture.value,skin.data.texture.signature));

                    updateSkin(player);

                    player.sendMessage(Utils.color("&aCosmetic Applied!"));
                }
            }.runTaskLater(main,75);
        }


        return false;
    }

    public void updateSkin(Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            if (!player.isOnline())
                return;

            try {
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                Location l = player.getLocation();

                PacketPlayOutPlayerInfo removeInfo;
                PacketPlayOutEntityDestroy removeEntity;
                PacketPlayOutNamedEntitySpawn addNamed;
                PacketPlayOutPlayerInfo addInfo;

                removeInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
                removeEntity = new PacketPlayOutEntityDestroy(entityPlayer.getId());

                addNamed = new PacketPlayOutNamedEntitySpawn(entityPlayer);

                addInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);

                World world = entityPlayer.getWorld();
                EnumDifficulty difficulty = entityPlayer.getWorld().getDifficulty();
                WorldData worlddata = entityPlayer.getWorld().worldData;
                net.minecraft.server.v1_8_R3.WorldType worldtype = worlddata.getType();
                int dimension = world.worldProvider.getDimension();
                PlayerInteractManager playerIntManager = entityPlayer.playerInteractManager;
                WorldSettings.EnumGamemode enumGamemode = playerIntManager.getGameMode();

                PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(dimension, difficulty, worldtype, enumGamemode);

                PacketPlayOutPosition pos = new PacketPlayOutPosition(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch(), new HashSet<>());

                Packet hand = new PacketPlayOutEntityEquipment(player.getEntityId(), 0, CraftItemStack.asNMSCopy(player.getItemInHand()));
                Packet helmet = new PacketPlayOutEntityEquipment(player.getEntityId(), 4, CraftItemStack.asNMSCopy(player.getInventory().getHelmet()));
                Packet chestplate = new PacketPlayOutEntityEquipment(player.getEntityId(), 3, CraftItemStack.asNMSCopy(player.getInventory().getChestplate()));
                Packet leggings = new PacketPlayOutEntityEquipment(player.getEntityId(), 2, CraftItemStack.asNMSCopy(player.getInventory().getLeggings()));
                Packet boots = new PacketPlayOutEntityEquipment(player.getEntityId(), 1, CraftItemStack.asNMSCopy(player.getInventory().getBoots()));
                PacketPlayOutHeldItemSlot slot = new PacketPlayOutHeldItemSlot(player.getInventory().getHeldItemSlot());

                for (Player pOnline : Bukkit.getOnlinePlayers()) {
                    CraftPlayer craftHandle = ((CraftPlayer) pOnline);
                    PlayerConnection playerCon = craftHandle.getHandle().playerConnection;

                    if (pOnline.equals(player)) {
                        playerCon.sendPacket(removeInfo);
                        playerCon.sendPacket(addInfo);
                        playerCon.sendPacket(respawn);

                        playerCon.sendPacket(pos);
                        playerCon.sendPacket(slot);

                        craftHandle.updateScaledHealth();
                        craftHandle.updateInventory();

                        if (pOnline.isOp()) {
                            pOnline.setOp(false);
                            pOnline.setOp(true);
                        }
                        continue;
                    }
                    /* Now checks if the player is in the same world and if can see the player
                     * I did that to try to prevent player duplications.
                     */
                    if (pOnline.getWorld().equals(player.getWorld()) && pOnline.canSee(player) && player.isOnline()) {
                        playerCon.sendPacket(removeEntity);
                        playerCon.sendPacket(removeInfo);
                        playerCon.sendPacket(addInfo);
                        playerCon.sendPacket(addNamed);

                        playerCon.sendPacket(hand);

                        playerCon.sendPacket(helmet);
                        playerCon.sendPacket(chestplate);
                        playerCon.sendPacket(leggings);
                        playerCon.sendPacket(boots);
                    } else {
                        //Only sends player update packet
                        playerCon.sendPacket(removeInfo);
                        playerCon.sendPacket(addInfo);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
