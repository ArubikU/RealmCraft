package dev.arubik.realmcraft.Api.Listeners;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.event.armorequip.ArmorEquipEvent;
import io.lumine.mythic.lib.api.event.armorequip.ArmorType;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockInfo;
import net.Indyuce.mmocore.manager.RestrictionManager.ToolPermissions;

public class ChangeGamemode implements Listener, Depend {
    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if (!event.getNewGameMode().equals(GameMode.SURVIVAL))
            return;
        for (int i = 0; i < event.getPlayer().getInventory().getSize(); i++) {
            ItemStack item = event.getPlayer().getInventory().getItem(i);
            if (item == null)
                continue;
            if (!item.hasItemMeta() || item.getItemMeta() == null)
                continue;
            if (!RealNBT.fromItemStack(item).contains("ORIGINAL_ITEM"))
                continue;
            ItemStack[] items = RealNBT.fromItemStack(item).getItemStackArray("ORIGINAL_ITEM");
            if (items == null)
                continue;
            event.getPlayer().getInventory().setItem(i, RealNBT.fromItemStack(items[0]).getItemStack());
        }

    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        // clear all the attributes
        for (Attribute attribute : Attribute.values()) {
            if (event.getPlayer().getAttribute(attribute) == null)
                continue;
            event.getPlayer().getAttribute(attribute).getModifiers().forEach(modifier -> {
                event.getPlayer().getAttribute(attribute).removeModifier(modifier);
            });
        }
    }

    @EventHandler
    public void onPlayerDisconenct(org.bukkit.event.player.PlayerQuitEvent event) {
        // clear all the attributes
        for (Attribute attribute : Attribute.values()) {
            if (event.getPlayer().getAttribute(attribute) == null)
                continue;
            event.getPlayer().getAttribute(attribute).getModifiers().forEach(modifier -> {
                event.getPlayer().getAttribute(attribute).removeModifier(modifier);
            });
        }
    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] {};
    }

    public static void register() {
        ChangeGamemode ml = new ChangeGamemode();
        Bukkit.getServer().getPluginManager().registerEvents(ml, realmcraft.getInstance());
        RealMessage.Found("Vanilla Events Listener Loaded");
    }

    // on player interact right click
    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() == null)
                return;
            if (!event.getItem().hasItemMeta())
                return;
            // verify if item is not a helmet or carved pumpkin
            if (event.getItem().getType().toString().contains("HELMET")
                    || event.getItem().getType().toString().contains("PUMPKIN"))
                return;
            RealNBT nbt = RealNBT.fromItemStack(event.getItem());
            if (nbt.contains("HELMET_ABLE")) {
                // verify if helmet is not already equipped
                if (event.getPlayer().getInventory().getHelmet() != null)
                    return;

                ItemStack helmet = event.getItem().clone();
                helmet.setAmount(1);
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(event.getPlayer(),
                        ArmorEquipEvent.EquipMethod.HOTBAR, ArmorType.HELMET, null, helmet);
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                if (armorEquipEvent.isCancelled())
                    return;
                event.getPlayer().getInventory().setHelmet(helmet);
                // play sound
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
                // take 1 from main hand
                if (event.getItem().getAmount() > 1) {
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                } else {
                    event.getPlayer().getInventory().setItemInMainHand(null);
                }

            }

        }
    }

    // on player consume an item
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerConsume(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        if (event.getItem() == null)
            return;
        if (!event.getItem().hasItemMeta())
            return;
        RealNBT nbt = RealNBT.fromItemStack(event.getItem());
        if (nbt.getBoolean("MMOITEMS_INEDIBLE", false)) {
            event.setCancelled(true);
        }
    }

    // if item tag "MMOITEMS_TYPE" == "THRUSTING_SWORD" cancel Smithing table
    // upgrade from diamond to netherite
    @EventHandler
    public void onSmithingTableUpgrade(org.bukkit.event.inventory.PrepareSmithingEvent event) {
        if (event.getInventory().getStorageContents().length == 0)
            return;
        if (event.getInventory().getStorageContents()[0] == null)
            return;
        if (!event.getInventory().getStorageContents()[0].hasItemMeta())
            return;
        RealNBT nbt = RealNBT.fromItemStack(event.getInventory().getStorageContents()[0]);

        List<String> ids = realmcraft.getInteractiveConfig().getStringList("blocked-smithng", null);
        String id = nbt.getString("MMOITEMS_ITEM_TYPE", "") + ":" + nbt.getString("MMOITEMS_ITEM_ID", "");
        if (nbt.getString("MMOITEMS_ITEM_TYPE", "").equalsIgnoreCase("THRUSTING_SWORD")
                || ids.contains(id)) {
            event.getInventory().setResult(null);
            event.setResult(null);
        }
    }

    // give 30 sec of inmortality when player join
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinInmortality(org.bukkit.event.player.PlayerJoinEvent event) {
        event.getPlayer().setInvulnerable(true);
        Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
            event.getPlayer().setInvulnerable(false);
        }, 600);
    }

    // verify death event and cancel if player is inmortal
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        if (event.getEntity().isInvulnerable()) {
            event.setCancelled(true);
        }
    }

}
