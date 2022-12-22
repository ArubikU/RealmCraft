package dev.arubik.realmcraft.Handlers;

import dev.arubik.realmcraft.realmcraft;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class RealMessage {

    private static BukkitAudiences bukkitAudiences = BukkitAudiences.create(realmcraft.getInstance());
    private static Audience console = bukkitAudiences.console();

    public static void sendConsoleMessage(String message) {
        console.sendMessage(realmcraft.getMiniMessage().deserialize(message));
    }
}
