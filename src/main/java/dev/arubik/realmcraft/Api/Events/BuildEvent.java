package dev.arubik.realmcraft.Api.Events;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.RealNBT;
import lombok.Getter;

public class BuildEvent extends Event implements Cancellable {

    @Override
    public @NotNull HandlerList getHandlers() {
        return new HandlerList();
    }

    @Getter
    RealNBT item;
    @Getter
    private final String id;
    @Getter
    private final String namespace;

    public BuildEvent(RealNBT item, String id, String namespace) {
        this.item = item;
        this.id = id;
        this.namespace = namespace;
    }

    public void CallEvent() {
        Bukkit.getServer().getPluginManager().callEvent(this);
    }

    Boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        cancelled = arg0;
    }

}
