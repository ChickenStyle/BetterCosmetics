package me.chickenstyle.cosmetics.utilities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.UUID;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import javax.swing.*;


public class Utils {

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&',text);
    }

    //BufferedImage
    public static BufferedImage getPlayerSkin(UUID uuid) throws IOException {
        String sURL = "https://api.mineskin.org/generate/user/" + uuid.toString().replace("-", ""); //just a string
        System.out.println(sURL);
        // Connect to the URL using java's native library
        URL url = new URL(sURL);
        URLConnection request = url.openConnection();
        request.connect();

        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
        JsonObject textures = rootobj.getAsJsonObject("data").getAsJsonObject("texture");
        String skinUrl = textures.get("url").getAsString();


        return ImageIO.read(new URL(skinUrl));

    }

    public static File applyCosmetic(UUID uuid, File file) throws IOException {
        BufferedImage playerSkin = getPlayerSkin(uuid);
        BufferedImage cosmetic = ImageIO.read(file);

        BufferedImage newSkin = overlayImages(playerSkin, cosmetic);

        File skinFile = new File("skin.png");
        ImageIO.write(newSkin, "png", skinFile);

        return skinFile;

    }

    public static BufferedImage overlayImages(BufferedImage bgImage,
                                              BufferedImage fgImage) {

        /**
         * Doing some preliminary validations.
         * Foreground image height cannot be greater than background image height.
         * Foreground image width cannot be greater than background image width.
         *
         * returning a null value if such condition exists.
         */
        if (fgImage.getHeight() > bgImage.getHeight()
                || fgImage.getWidth() > fgImage.getWidth()) {
            JOptionPane.showMessageDialog(null,
                    "Foreground Image Is Bigger In One or Both Dimensions"
                            + "nCannot proceed with overlay."
                            + "nn Please use smaller Image for foreground");
            return null;
        }

        /**Create a Graphics  from the background image**/
        Graphics2D g = bgImage.createGraphics();
        /**Set Antialias Rendering**/
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        /**
         * Draw background image at location (0,0)
         * You can change the (x,y) value as required
         */
        g.drawImage(bgImage, 0, 0, null);

        /**
         * Draw foreground image at location (0,0)
         * Change (x,y) value as required.
         */
        g.drawImage(fgImage, 0, 0, null);

        g.dispose();

        return bgImage;
    }
}
