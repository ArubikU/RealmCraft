package dev.arubik.realmcraft.Managers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.mechanics.BoneMealMechanic;
import io.lumine.mythic.lib.api.player.EquipmentSlot;

public class Rat implements Listener {
    // on villager break crop
    // 10% chance to spawn rat
    // 5% of the rat will be plague rat
    boolean enabled = true;
    boolean plagueRatenabled = false;
    int plagueRatChance = 5;
    int ratChance = 10;
    String rat = "normalrat";
    String plagueRat = "plaguerat";

    @EventHandler
    public void onVillagerBreakCrop(EntityChangeBlockEvent event) {
        if (!enabled)
            return;
        if (event.getEntity() instanceof Villager villager) {
            if (event.getBlock().getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    if (Utils.Chance(ratChance, 100)) {
                        Location loc = event.getBlock().getLocation();
                        if (plagueRatenabled) {
                            if (Utils.Chance(plagueRatChance, 100)) {
                                MythicBukkit.inst().getMobManager().spawnMob(plagueRat, loc, 1);
                                return;
                            }
                        }
                        MythicBukkit.inst().getMobManager().spawnMob(rat, loc, 1);

                    }
                }
            }
        }
    }

    public static void Setup() {
        Rat mimic = new Rat();
        Bukkit.getServer().getPluginManager().registerEvents(mimic, realmcraft.getInstance());
        RealMessage.sendConsoleMessage("Rat Manager Loaded");
    }

    // on animal eat grass

    // on use bone meal on crop
    @EventHandler(priority = EventPriority.LOW)
    public void onFertilize(BlockFertilizeEvent event) {
        if (!enabled)
            return;

        ItemStack item = null;
        Player player = event.getPlayer();
        EquipmentSlot hand = EquipmentSlot.MAIN_HAND;
        if (player == null)
            return;
        if (player.getInventory().getItemInMainHand().getType() == Material.BONE_MEAL) {
            item = player.getInventory().getItemInMainHand();
        } else if (player.getInventory().getItemInOffHand().getType() == Material.BONE_MEAL) {
            item = player.getInventory().getItemInOffHand();
            hand = EquipmentSlot.OFF_HAND;
        }

        if (item == null)
            return;

        RealNBT nbt = new RealNBT(item);
        if (nbt.hasTag("manure")) {
            int manure = nbt.getInt("manure");
            if (event.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Sapling sap) {

                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(hand.toBukkit(), item);
                // event.setCancelled(true);
                sap.setStage(sap.getMaximumStage());
                event.getBlock().setBlockData(sap);

            }
            if (event.getBlock().getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
                int newAge = ageable.getAge() + manure;
                if (newAge > ageable.getMaximumAge()) {
                    newAge = ageable.getMaximumAge();
                }
                ageable.setAge(newAge);
                Block block = event.getBlock();
                block.setBlockData(ageable);
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(hand.toBukkit(), item);
                event.setCancelled(true);

            }
        }
    }

}
