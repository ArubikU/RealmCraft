package dev.arubik.realmcraft.IReplacer;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.world.LootGenerateEvent;

import dev.arubik.realmcraft.FileManagement.InteractiveSection;
import io.papermc.paper.event.player.PlayerTradeEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory;

public class ReplacementContext {

    public enum GenerationType {
        NATURALLY,
        MANUALLY
    }

    @Getter
    @Setter
    @Nullable
    public Location location;
    @Getter
    @Setter
    @Nullable
    public Entity entity;

    @Getter
    @Setter
    public Event event;

    @Getter
    @Setter
    public GenerationType genType = GenerationType.MANUALLY;

    // EntityPickupItemEvent
    public static ReplacementContext ofEvent(EntityPickupItemEvent event) {
        ReplacementContext context = new ReplacementContext();
        context.setEntity(event.getEntity());
        context.setLocation(event.getItem().getLocation());
        context.setEvent(event);
        return context;
    }

    // InventoryClickEvent
    public static ReplacementContext ofEvent(InventoryClickEvent event) {
        ReplacementContext context = new ReplacementContext();
        context.setEntity(event.getWhoClicked());
        context.setLocation(event.getClickedInventory().getLocation());
        context.setEvent(event);
        return context;
    }

    // InventoryMoveItemEvent
    public static ReplacementContext ofEvent(InventoryMoveItemEvent event) {
        ReplacementContext context = new ReplacementContext();
        if (event.getDestination().getHolder() instanceof Entity entity) {

            context.setEntity(entity);
        }
        context.setLocation(event.getDestination().getLocation());
        context.setEvent(event);
        return context;
    }

    // InventoryOpenEvent
    public static ReplacementContext ofEvent(InventoryOpenEvent event) {
        ReplacementContext context = new ReplacementContext();
        context.setEntity(event.getPlayer());
        context.setLocation(event.getPlayer().getLocation());
        context.setEvent(event);
        return context;
    }

    // PrepareItemCraftEvent
    public static ReplacementContext ofEvent(PrepareItemCraftEvent event) {
        ReplacementContext context = new ReplacementContext();
        context.setEntity(event.getViewers().get(0));
        context.setLocation(event.getInventory().getLocation());
        context.setEvent(event);
        return context;
    }

    // CraftItemEvent
    public static ReplacementContext ofEvent(CraftItemEvent event) {
        ReplacementContext context = new ReplacementContext();
        context.setEntity(event.getWhoClicked());
        context.setLocation(event.getInventory().getLocation());
        context.setEvent(event);
        return context;
    }

    // LootGenerateEvent
    public static ReplacementContext ofEvent(LootGenerateEvent event) {
        ReplacementContext context = new ReplacementContext();
        Entity looter = event.getEntity();
        if (looter == null && event.getInventoryHolder() instanceof Entity entity) {
            looter = entity;
        }
        context.setEntity(looter);
        context.setLocation(event.getLootContext().getLocation());
        if (event.getInventoryHolder() instanceof Container block) {
            context.setLocation(block.getLocation());
        }
        context.setGenType(GenerationType.NATURALLY);
        context.setEvent(event);
        return context;
    }

    // PlayerTradeEvent
    public static ReplacementContext ofEvent(PlayerTradeEvent event) {
        ReplacementContext context = new ReplacementContext();
        context.setEntity(event.getPlayer());
        context.setLocation(event.getVillager().getLocation());
        context.setEvent(event);
        return context;
    }

    // EntityPickupItemEvent
    public static ReplacementContext ofEvent(FurnaceSmeltEvent event) {
        ReplacementContext context = new ReplacementContext();
        context.setEntity(null);
        context.setLocation(event.getBlock().getLocation());
        context.setEvent(event);
        return context;
    }

}