package dev.arubik.realmcraft.Api.Events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.RealLore;

/*
 * This event is called when the plugin is ready to rebuild the lore of the items.
 * You can add your own lore to the event by calling addLore(RealLore lore) method.
 * You can add your own item build modifier to the event by calling addItemBuildModifier(ItemBuildModifier modifier) method. (v1.0.1)
 * 
 * path: src\main\java\dev\arubik\realmcraft\Api\Events\OnRebuildLoreListener.java
 */
public class OnRebuildLoreListener extends Event {
    public OnRebuildLoreListener() {
        super(true);
    }

    public static void CallEvent() {
        Bukkit.getScheduler().runTaskAsynchronously(realmcraft.getInstance(), () -> {
            Bukkit.getServer().getPluginManager().callEvent(new OnRebuildLoreListener());
        });
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return new HandlerList();
    }

    public void addLore(RealLore lore) {
        LoreEvent.addLore(lore);
    }

    public void addItemBuildModifier(ItemBuildModifier modifier) {
        LoreEvent.addItemBuildModifier(modifier);
    }
}
