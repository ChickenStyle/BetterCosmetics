package me.chickenstyle.cosmetics;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.chickenstyle.cosmetics.utilities.Utils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineskin.MineskinClient;
import org.mineskin.data.Skin;

import java.io.*;

import java.util.HashSet;
import java.util.UUID;


public class BetterCosmetics extends JavaPlugin implements Listener {
    private MineskinClient skinClient;
    private Skin skin;
    @Override
    public void onEnable() {
        skinClient = new MineskinClient("SusChamp","6d0249735555da9c0324c140fb1092d8e64d4ae3940a0b6c426ce2caf4c8711a");

        saveResource("CustomSkins/testskin.png",false);

        saveResource("CustomSkins/crown.png",false);

        saveResource("CustomSkins/glove.png",false);

        getCommand("applycosmetic").setExecutor(new CosmeticCommand(this));

        getServer().getPluginManager().registerEvents(this,this);

        System.out.println("s");



    }



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) throws IOException {









    }



    public static void main(String[] args) throws IOException {
        Utils.applyCosmetic(UUID.fromString("5f18003a-0428-43e8-86c7-ae22d73105ab"),new File("testskin.png")).createNewFile();
    }

    public MineskinClient getSkinClient() {
        return skinClient;
    }


}
