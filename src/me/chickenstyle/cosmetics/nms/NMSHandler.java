package me.chickenstyle.cosmetics.nms;

import org.bukkit.entity.Player;
import org.mineskin.data.Skin;

public interface NMSHandler {

    void applySkin(Player player, Skin skin);

    void refreshSkin(Player player);
}
