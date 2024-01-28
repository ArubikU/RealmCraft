package dev.arubik.realmcraft.Api.Events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class CustomEvent extends PlayerEvent implements Cancellable {

    public String key = "def";

    public CustomEvent(@NotNull Player who, String key) {
        super(who);
        this.key = key;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return new HandlerList();
    }

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
