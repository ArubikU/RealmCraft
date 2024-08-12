package dev.arubik.realmcraft.Managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealData;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.RealPotionEffect;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.LootGen.ContainerApi;
import dev.arubik.realmcraft.LootGen.LootTable;
import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import lombok.Getter;

public class BloodMoon implements Listener {

    public static Boolean enabled = false;
    public static Boolean lootTable = false;
    private static String lootTableName = "bloodmoon";
    private static double multiplier = 1.0;
    @Getter
    public static Boolean PermaNight = false;
    private static List<RealPotionEffect> potionOfMobs = new ArrayList();
    private static double chanceOfPotions = 0.0;
    private static Vector vector = new Vector(0, 0.1, 0);
    private static Vector variation = new Vector(0.07, 0.05, 0.07);
    private static List<String> worlds = new ArrayList();
    private static int taskID = 0;
    private static double chanceOfLoot = 30.0;
    private static boolean registered = false;

    public static void Reload() {
        enabled = realmcraft.getInteractiveConfig().getBoolean("bloodmoon.enabled", enabled);
        lootTable = realmcraft.getInteractiveConfig().getBoolean("bloodmoon.loottable", lootTable);
        lootTableName = realmcraft.getInteractiveConfig().getString("bloodmoon.loottablename", lootTableName);
        if (!LootTable.exist(lootTableName)) {
            lootTable = false;
        }
        multiplier = realmcraft.getInteractiveConfig().getDouble("bloodmoon.multiplier", multiplier);
        PermaNight = realmcraft.getInteractiveConfig().getBoolean("bloodmoon.permanight", PermaNight);
        potionOfMobs = RealData.PotionEffectFromList(
                realmcraft.getInteractiveConfig().getStringList("bloodmoon.potionofmobs", RealNBT.EmptyList()));
        chanceOfPotions = realmcraft.getInteractiveConfig().getDouble("bloodmoon.chancepotions", chanceOfPotions);
        worlds = realmcraft.getInteractiveConfig().getStringList("bloodmoon.worlds", worlds);

        if (taskID != 0) {
            Bukkit.getScheduler().cancelTask(taskID);
        }

        if (PermaNight && enabled) {
            for (String world : worlds) {
                Bukkit.getWorld(world).setTime(18000);
            }
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(realmcraft.getInstance(), new Runnable() {
                @Override
                public void run() {
                    for (String world : worlds) {
                        Bukkit.getWorld(world).setTime(18000);
                    }
                }
            }, 0, 100);
            taskID = task.getTaskId();
        }

        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new BloodMoon(), realmcraft.getInstance());
            registered = true;
        }

    }

    @EventHandler
    public void onDealDamageByMob(EntityDamageByEntityEvent event) {
        if (enabled) {
            if (event.getEntity() instanceof Player) {
                if (event.getDamager() instanceof LivingEntity) {
                    LivingEntity entity = (LivingEntity) event.getDamager();
                    // verify if entity is hostile or neutral
                    if (entity.isDead() || entity.isInvulnerable() || entity.isCustomNameVisible()
                            || entity.isCustomNameVisible()) {
                        return;
                    }
                    if (entity instanceof Monster) {
                        event.setDamage(event.getDamage() * multiplier);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpawnMob(CreatureSpawnEvent event) {
        if (enabled) {
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                if (event.getEntity() instanceof Monster) {
                    if (Utils.Chance(chanceOfPotions, 100)) {
                        for (RealPotionEffect effect : potionOfMobs) {
                            effect.apply(event.getEntity());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeathMob(PlayerKillEntityEvent event) {
        if (enabled) {
            if (event.getTarget() instanceof Monster) {
                double chance = chanceOfLoot;
                // get fortune level from player
                int fortuneLevel = event.getPlayer().getInventory().getItemInMainHand()
                        .getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
                chance = chance * (1 + fortuneLevel * 0.1);

                if (Utils.Chance(chance, 100)) {
                    if (!event.getTarget().hasMetadata("fromSpawner")) {
                        if (lootTable) {

                            ContainerApi.dropLoot(lootTableName, event.getTarget().getLocation(), InventoryType.BARREL,
                                    vector, variation);
                        }
                    }
                }
            }
        }
    }

}
