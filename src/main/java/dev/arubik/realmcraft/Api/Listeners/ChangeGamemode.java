package dev.arubik.realmcraft.Api.Listeners;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.compatibility.MMOItemsSupport.MMOItemsBlock;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.event.armorequip.ArmorEquipEvent;
import io.lumine.mythic.lib.api.event.armorequip.ArmorType;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockInfo;
import net.Indyuce.mmocore.manager.RestrictionManager.ToolPermissions;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.MMOItemsAPI;
import net.seyarada.pandeloot.Constants;

public class ChangeGamemode implements Listener, Depend {
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

    // on interact with armor stand verify if armor stand helmet slot is empty and
    // if is equip the helmet
    @EventHandler
    public void onArmorStandInteract(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand stand) {
            if (stand.getEquipment().getHelmet() != null)
                return;
            ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
            if (item == null)
                return;
            if (!item.hasItemMeta())
                return;
            RealNBT nbt = RealNBT.fromItemStack(item);
            if (nbt.contains("HELMET_ABLE")) {
                ItemStack helmet = item.clone();
                helmet.setAmount(1);
                PlayerArmorStandManipulateEvent armorStandManipulateEvent = new PlayerArmorStandManipulateEvent(
                        event.getPlayer(), stand, helmet, null, EquipmentSlot.HEAD, event.getHand());

                Bukkit.getServer().getPluginManager().callEvent(armorStandManipulateEvent);
                if (armorStandManipulateEvent.isCancelled())
                    return;

                stand.getEquipment().setHelmet(helmet);
                // play sound
                stand.getLocation().getWorld().playSound(stand.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
                // take 1 from main hand
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    event.getPlayer().getInventory().setItem(event.getHand(), item);
                } else {
                    event.getPlayer().getInventory().setItem(event.getHand(), null);
                }

            }

        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
        if (event.getRightClicked().getEquipment().getHelmet() != null)
            return;
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (item == null)
            return;
        if (!item.hasItemMeta())
            return;
        RealNBT nbt = RealNBT.fromItemStack(item);
        if (nbt.contains("HELMET_ABLE")) {
            ItemStack helmet = item.clone();
            helmet.setAmount(1);
            event.setCancelled(true);
            event.getRightClicked().getEquipment().setHelmet(helmet);
            // play sound
            event.getRightClicked().getLocation().getWorld().playSound(event.getRightClicked().getLocation(),
                    Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
            // take 1 from main hand
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                event.getPlayer().getInventory().setItem(event.getHand(), item);
            } else {
                event.getPlayer().getInventory().setItem(event.getHand(), null);
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

    // listen item pickup event to fix stacking items not stackin
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            // verify if item is stackable
            if (event.getItem().getItemStack().hasItemMeta()
                    && (event.getItem().getItemStack().getItemMeta().getPersistentDataContainer()).has(
                            Constants.LOOTBAG_KEY,
                            PersistentDataType.STRING))
                return;
            event.setCancelled(true);
            player.getInventory().addItem(event.getItem().getItemStack());
            event.getItem().setVelocity(new Vector(0, 0.2, 0));
            event.getItem().remove();
            // play sound
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);

        }
    }

    // on player break block and if block id from mmoitems is in config get the
    // amount of xp will be drop depending of formula
    // <total xp> = <block xp> + <block xp> * <fortune level> / 10
    // that method use vanilla event
    // @EventHandler(priority = EventPriority.LOWEST)
    // public void onMMOBlockBreakEvent(BlockBreakEvent event) {
    // if
    // (!MMOItems.plugin.getCustomBlocks().isMushroomBlock(event.getBlock().getType()))
    // return;
    // MMOItems.plugin.getCustomBlocks().getFromBlock(event.getBlock().getBlockData()).ifPresent(block
    // -> {
    // realmcraft.getInteractiveConfig().ifPresent("block." + block.getId(),
    // String.class, value -> {
    // // value can be a range like 10to20
    // final int xp;
    //
    // if (value.contains("to")) {
    // String[] values = value.split("to");
    // xp = new Random().nextInt(Integer.parseInt(values[0]),
    // Integer.parseInt(values[1]));
    // } else {
    // xp = Integer.parseInt(value);
    // }
    // // add fortune level
    // // summon xp orb
    // event.getBlock().getWorld().spawn(event.getBlock().getLocation(),
    // ExperienceOrb.class, orb -> {
    // orb.setExperience(xp + (xp *
    // event.getPlayer().getInventory().getItemInMainHand()
    // .getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) / 20));
    // });
    //
    // });
    // });
    // }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.LIGHTNING) {
            if (event.getEntity() instanceof LivingEntity living) {
                Double damage = event.getDamage();
                event.setDamage(0);
                DamageMetadata metadata = new DamageMetadata();
                metadata.add(damage, Element.valueOf("THUNDER"), DamageType.MAGIC);
                AttackMetadata damageme = new AttackMetadata(metadata, living, StatProvider.get(living));
                MythicLib.plugin.getDamage().damage(damageme, living);
            }
        }
    }
}
