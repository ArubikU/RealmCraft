package dev.arubik.realmcraft.Handlers;

import java.time.Duration;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ehhthan.happyhud.HappyHUD;
import com.ehhthan.happyhud.api.HudHolder;
import com.ehhthan.happyhud.api.element.popup.HudPopup;

import dev.arubik.realmcraft.realmcraft;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

public class RealMessage {

    private static BukkitAudiences bukkitAudiences = BukkitAudiences.create(realmcraft.getInstance());
    private static Audience console = bukkitAudiences.console();

    public static String deserialize(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    public static void sendConsoleMessage(String message) {
        console.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void sendConsoleMessage(String message, String... args) {
        message = PlaceholderConfigParser.parser(message);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("<" + i + ">", args[i]);
        }
        console.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void sendRaw(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }

    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    public enum DebugType {
        DEBUG, INFO, WARNING, ERROR, LOREPARSER, ANVILREPAIR, DEPENDECY, MODIFYLEVEL, IREPLACER
    }

    public static void sendConsoleMessage(String debugType, String message, String... args) {
        if (realmcraft.getInteractiveConfig().getBoolean("debug." + debugType, false)) {
            message = PlaceholderConfigParser.parser(message);
            for (int i = 0; i < args.length; i++) {
                message = message.replace("<" + i + ">", args[i]);
            }
            console.sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
    }

    public static void sendConsoleMessage(String debugType, String message) {
        if (realmcraft.getInteractiveConfig().getBoolean("debug." + debugType, false)) {
            console.sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
    }

    public static void sendConsoleMessage(DebugType debugType, String message, String... args) {
        sendConsoleMessage(debugType.toString(), message, args);
    }

    public static void sendConsoleMessage(DebugType debugType, String message) {
        sendConsoleMessage(debugType.toString(), message);
    }

    public static boolean isDebugEnabled(DebugType type) {
        return realmcraft.getInteractiveConfig().getBoolean("debug." + type.toString(), false);
    }

    public static void nonFound(String message) {
        if (realmcraft.getInteractiveConfig().getBoolean("debug." + DebugType.DEPENDECY, true)) {
            console.sendMessage(MiniMessage.miniMessage().deserialize("<red>[DepedencyError] " + message));
        }
    }

    public static void Found(String message) {
        if (realmcraft.getInteractiveConfig().getBoolean("debug." + DebugType.DEPENDECY, true)) {
            console.sendMessage(MiniMessage.miniMessage().deserialize("<green>[DepedencyFounded] " + message));
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        message = PlaceholderConfigParser.parser(message);
        bukkitAudiences.sender(sender).sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void sendMessage(CommandSender sender, String message, String... args) {
        message = PlaceholderConfigParser.parser(message);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("<" + i + ">", args[i]);
        }
        bukkitAudiences.sender(sender).sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void alert(String message) {
        message = PlaceholderConfigParser.parser(message);
        message = "<yellow>[RealmCraft] " + message;
        bukkitAudiences.console().sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void sendActionBar(Player player, String message) {
        message = PlaceholderConfigParser.parser(message);

        bukkitAudiences.sender(player).sendActionBar(MiniMessage.miniMessage().deserialize(message));
    }

    public static void sendTittle(Player player, String title, String subtitle, long fadeIn, long stay, long fadeOut) {
        title = PlaceholderConfigParser.parser(title);
        subtitle = PlaceholderConfigParser.parser(subtitle);
        Title tilemodel = Title.title(MiniMessage.miniMessage().deserialize(title),
                MiniMessage.miniMessage().deserialize(subtitle),
                Title.Times.of(Duration.ofSeconds(fadeIn), Duration.ofSeconds(stay), Duration.ofSeconds(fadeOut)));
        bukkitAudiences.player(player).showTitle(tilemodel);
    }

    public static void sendTittle(Player player, String title, String subtitle) {
        title = PlaceholderConfigParser.parser(title);
        subtitle = PlaceholderConfigParser.parser(subtitle);
        Title tilemodel = Title.title(MiniMessage.miniMessage().deserialize(title),
                MiniMessage.miniMessage().deserialize(subtitle));
        bukkitAudiences.player(player).showTitle(tilemodel);
    }

    // now withoyt subtitle

    public static void sendTittle(Player player, String title) {
        title = PlaceholderConfigParser.parser(title);
        Title tilemodel = Title.title(MiniMessage.miniMessage().deserialize(title),
                MiniMessage.miniMessage().deserialize(""));
        bukkitAudiences.player(player).showTitle(tilemodel);
    }

    public static void sendTittle(Player player, String title, long fadeIn, long stay, long fadeOut) {
        title = PlaceholderConfigParser.parser(title);
        String subtitle = PlaceholderConfigParser.parser("");
        Title tilemodel = Title.title(MiniMessage.miniMessage().deserialize(title),
                MiniMessage.miniMessage().deserialize(subtitle),
                Title.Times.of(Duration.ofSeconds(fadeIn), Duration.ofSeconds(stay), Duration.ofSeconds(fadeOut)));
        bukkitAudiences.player(player).showTitle(tilemodel);
    }

    private static Object popup = null;

    public static void PopUp(Player player, String message) {
        if (popup == null) {
            if (Bukkit.getPluginManager().getPlugin("HappyHud") != null) {
                popup = HappyHUD.getInstance().popups().get("death-message");
            } else {
                popup = "temp";
            }
        }
        if (popup.equals("temp")) {
            throw new NullPointerException("Popup is null");

        }

        HudHolder.get(player).sendPopup((HudPopup) popup, 100, message);
    }
}
