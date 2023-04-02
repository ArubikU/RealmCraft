package dev.arubik.realmcraft.Handlers;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.georgev22.skinoverlay.SkinOverlay;
import com.georgev22.skinoverlay.gson.JsonArray;
import com.georgev22.skinoverlay.gson.JsonElement;
import com.georgev22.skinoverlay.gson.JsonObject;
import com.georgev22.skinoverlay.gson.JsonParser;
import com.georgev22.skinoverlay.library.maps.HashObjectMap;
import com.georgev22.skinoverlay.utilities.MessagesUtil;
import com.georgev22.skinoverlay.utilities.Utilities;
import com.georgev22.skinoverlay.utilities.Utilities.Request;
import com.georgev22.skinoverlay.utilities.interfaces.ImageSupplier;
import com.georgev22.skinoverlay.utilities.player.PlayerObject;
import com.georgev22.skinoverlay.utilities.player.PlayerObjectBukkit;
import com.georgev22.skinoverlay.utilities.player.User;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.Managers.Depend;
import lombok.Getter;

public class Overlay implements Listener, Depend {
    public static void addLayer(Player player, String layer)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
            IOException {
        PlayerObject playerObject = new PlayerObjectBukkit(player);
        SkinOverlay skinOverlay = SkinOverlay.getInstance();

        BufferedImage imageSupplier = ImageIO
                .read(new File(skinOverlay.getSkinsDataFolder(), layer + ".png"));

        skinOverlay.getUserManager().getUser(player.getUniqueId()).handle((user, throwable) -> {

            if (throwable != null) {
                skinOverlay.getLogger().log(Level.SEVERE, "Error retrieving user: ", throwable);
                return null;
            }
            return user;
        }).thenApplyAsync(user -> {

            if (user == null) {
                skinOverlay.getLogger().log(Level.SEVERE, "Error retrieving user");
                return null;
            }
            Image overlay;
            overlay = imageSupplier;
            try {
                byte[] profileBytes = skinOverlay.getSkinHandler().getProfileBytes(playerObject,
                        user.getCustomData("skinProperty"));
                JsonElement json = JsonParser.parseString(new String(profileBytes));
                JsonArray properties = json.getAsJsonObject().get("properties").getAsJsonArray();
                for (JsonElement object : properties) {
                    if (!object.getAsJsonObject().get("name").getAsString().equals("textures"))
                        continue;
                    if (overlay == null) {
                        GameProfile gameProfile = skinOverlay.getSkinHandler().getGameProfile(playerObject);
                        PropertyMap pm = gameProfile.getProperties();
                        Property property = pm.get("textures").stream()
                                .filter(gameProfileProperty -> gameProfileProperty.getName().equals("textures"))
                                .findFirst().orElseThrow();
                        pm.remove("textures", property);
                        pm.put("textures", new Property("textures", object.getAsJsonObject().get("value").getAsString(),
                                object.getAsJsonObject().get("signature").getAsString()));
                        user.addCustomData("skinName", layer);
                        user.addCustomData("skinProperty",
                                new Property("textures", object.getAsJsonObject().get("value").getAsString(),
                                        object.getAsJsonObject().get("signature").getAsString()));
                        break;
                    }
                    String base64 = object.getAsJsonObject().get("value").getAsString();
                    String value = new String(Base64.getDecoder().decode(base64));
                    JsonElement textureJson = JsonParser.parseString(value);
                    String skinUrl = textureJson.getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN")
                            .get("url").getAsString();
                    BufferedImage skin = ImageIO.read(new URL(skinUrl));
                    BufferedImage image = new BufferedImage(skin.getWidth(), skin.getHeight(), 2);
                    Graphics2D canvas = image.createGraphics();
                    canvas.drawImage(skin, 0, 0, null);
                    canvas.drawImage(overlay, 0, 0, null);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    ImageIO.write(image, "PNG", stream);
                    canvas.dispose();
                    String boundary = "*****";
                    String crlf = "\r\n";
                    String twoHyphens = "--";
                    Request request = new Request()
                            .openConnection("https://api.mineskin.org/generate/upload?visibility=1")
                            .postRequest()
                            .setRequestProperty("Content-Type", ("multipart/form-data;boundary=" + boundary))
                            .writeToOutputStream(twoHyphens + boundary + crlf,
                                    "Content-Disposition: form-data; name=\"file\";filename=\"file.png\"" + crlf, crlf)
                            .writeToOutputStream(new byte[][] { stream.toByteArray() })
                            .writeToOutputStream(crlf, twoHyphens + boundary + twoHyphens + crlf)
                            .closeOutputStream()
                            .finalizeRequest();
                    switch (request.getHttpCode()) {
                        case 429 -> skinOverlay.getLogger().log(Level.SEVERE, "Too many requests");
                        case 200 -> {
                            JsonElement response = JsonParser.parseString(new String(request.getBytes()));
                            JsonObject texture = response.getAsJsonObject().getAsJsonObject("data")
                                    .getAsJsonObject("texture");
                            String texturesValue = texture.get("value").getAsString();
                            String texturesSignature = texture.get("signature").getAsString();
                            user.addCustomData("skinName", layer);
                            user.addCustomData("skinProperty",
                                    new Property("textures", texturesValue, texturesSignature));

                        }
                        default ->
                            skinOverlay.getLogger().log(Level.SEVERE, "Unknown error code: " + request.getHttpCode());
                    }
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
            return user;
        }).thenAccept(user -> {
            if (user != null)
                Utilities.updateSkin(playerObject, true);
            else
                skinOverlay.getLogger().log(Level.SEVERE, "User is null");
        });
    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "SkinOverlay" };
    }

    public static void register() {
        if (Depend.isPluginEnabled(new Overlay())) {

            Bukkit.getPluginManager().registerEvents(new Overlay(), realmcraft.getInstance());
            RealMessage.Found("SkinOverlay found OveralyEnhancer enabled");
        } else {
            RealMessage.nonFound("SkinOverlay not found");
        }
    }

    @Getter
    private static InteractiveFile file = new InteractiveFile("overlay.yml", realmcraft.getInstance());
    static {
        file.setAutoUpdate(true);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        PlayerObject playerObject = new PlayerObjectBukkit(player);
        SkinOverlay.getInstance().getUserManager().getUser(player.getUniqueId()).thenApply((user) -> {

            try {
                byte[] profileBytes = SkinOverlay.getInstance().getSkinHandler().getProfileBytes(playerObject,
                        user.getCustomData("skinProperty"));
                String value = new String(Base64.getDecoder().decode(profileBytes));
                file.set(player.getUniqueId().toString(), value);
            } catch (IOException | ExecutionException | InterruptedException e1) {
                // e1.printStackTrace();
            }

            return user;
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerObject playerObject = new PlayerObjectBukkit(player);
        SkinOverlay.getInstance().getUserManager().getUser(player.getUniqueId()).thenApply((user) -> {
            String value = file.getString(player.getUniqueId().toString());
            if (value == null)
                return user;
            byte[] profileBytes = Base64.getEncoder().encode(value.getBytes());
            user.addCustomData("skinProperty", profileBytes);
            Utilities.updateSkin(playerObject, true);

            return user;
        });
    }

}
