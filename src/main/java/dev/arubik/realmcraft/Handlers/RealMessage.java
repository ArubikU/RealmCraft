package dev.arubik.realmcraft.Handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.arubik.realmcraft.realmcraft;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class RealMessage {

    private static BukkitAudiences bukkitAudiences = BukkitAudiences.create(realmcraft.getInstance());
    private static Audience console = bukkitAudiences.console();

    public static void sendConsoleMessage(String message) {
        console.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void sendRaw(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }

    public static void sendRaw(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            if (player.isOp()) {
                // sender.sendMessage(message);
            }
        } else {
            sender.sendMessage(message);
        }
    }

    public enum DebugType {
        DEBUG, INFO, WARNING, ERROR, LOREPARSER, ANVILREPAIR, DEPENDECY, MODIFYLEVEL, IREPLACER
    }

    public static void sendConsoleMessage(String debugType, String message) {
        if (realmcraft.getInteractiveConfig().getBoolean("debug." + debugType, false)) {
            console.sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
    }

    public static void sendConsoleMessage(DebugType debugType, String message) {
        if (realmcraft.getInteractiveConfig().getBoolean("debug." + debugType, false)) {
            console.sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
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
}
