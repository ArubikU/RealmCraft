package dev.arubik.realmcraft.MythicMobs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Utils;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;

public class DropEquipment implements ITargetedEntitySkill {
    private double chance = 0.08;
    private String ploot = "MOB";

    private Boolean lockPlayer = true;
    private int ticksLocked = 200;
    private Boolean spread = false;

    private String type = "lb:";

    public DropEquipment(MythicLineConfig config) {
        chance = config.getDouble("chance", chance);
        ploot = config.getString("ploot", ploot);
        type = config.getString("type", type);
        lockPlayer = config.getBoolean("lockPlayer", lockPlayer);
        ticksLocked = config.getInteger("ticksLocked", ticksLocked);
        spread = config.getBoolean("spread", spread);
        ploot = type + ploot;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata arg0, AbstractEntity target) {

        LivingEntity LivingMob = (LivingEntity) BukkitAdapter.adapt(target);
        if (LivingMob.hasMetadata("fromSpawner"))
            return SkillResult.SUCCESS;
        if (realmcraft.getInteractiveConfig().getBoolean("ploot", false)) {
            Bukkit.getScheduler().runTask(realmcraft.getInstance(), () -> {

                double weight = 0;
                if (MythicBukkit.inst().getMobManager().isMythicMob(target)) {
                    weight = MythicBukkit.inst().getMobManager().getMythicMobInstance(target).getLevel();
                    if (weight > 90) {
                        weight = 10;
                    } else if (weight > 70) {
                        weight = 9;
                    } else if (weight > 50) {
                        weight = 7;
                    } else if (weight > 30) {
                        weight = 6;
                    } else if (weight > 10) {
                        weight = 5;
                    } else {
                        weight = 4;
                    }

                } else {
                    weight = 2;
                }
                weight *= 1.5;
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack item = LivingMob.getEquipment().getItem(slot);
                    if (item == null)
                        continue;
                    if (item.getType() == Material.AIR)
                        continue;

                    String tier = RealNBT.fromItemStack(item).getString("MMOITEMS_TIER", "COMMON");
                    Boolean Superier = false;
                    if (tier.equalsIgnoreCase("COMMON") || tier.equalsIgnoreCase("TRASH")) {
                        weight += 1;
                    } else if (tier.equalsIgnoreCase("UNCOMMON") || tier.equalsIgnoreCase("JUNK")) {
                        weight += 2;
                    } else if (tier.equalsIgnoreCase("RARE")) {
                        weight += 3;
                    } else if (tier.equalsIgnoreCase("VERY_RARE")) {
                        weight += 4;
                        Superier = true;
                    } else if (tier.equalsIgnoreCase("LEGENDARY")) {
                        weight += 6;
                        Superier = true;
                    } else if (tier.equalsIgnoreCase("UNIQUE") || tier.equalsIgnoreCase("MYTHICAL")) {
                        weight += 7;
                        Superier = true;
                    } else if (tier.equalsIgnoreCase("EPIC")) {
                        weight += 8;
                        Superier = true;
                    }

                    if (LivingMob.getLastDamageCause() != null) {
                        EntityDamageEvent event = LivingMob.getLastDamageCause();
                        if (event instanceof EntityDamageByEntityEvent eventTwo) {
                            LivingEntity entDamage = (LivingEntity) eventTwo.getDamager();
                            if (entDamage.getEquipment().getItemInMainHand() != null) {
                                RealNBT nbt = RealNBT.fromItemStack(entDamage.getEquipment().getItemInMainHand());
                                // fortune enchantment
                                int level = nbt.getEnchantmentLevel("LOOT_BONUS_MOBS");
                                // calc a formula to add to chance
                                chance += level * 0.04;
                            }
                        }
                        chance *= 100;
                        if (Utils.Chance(chance, 100)) {
                            if (event instanceof EntityDamageByEntityEvent eventTwo) {
                                Entity killer = eventTwo.getDamager();
                                if (killer instanceof Player player) {
                                    ItemStack a = item;
                                    if (Superier) {
                                        a = RealNBT.fromItemStack(item).regenerate(player, weight * 6.5);
                                    } else {
                                        a = RealNBT.fromItemStack(item).regenerate(player, weight * 2.5);
                                    }
                                    LivingMob.getWorld().dropItem(LivingMob.getLocation(), a);
                                } else {
                                    LivingMob.getWorld().dropItem(LivingMob.getLocation(), item);
                                }
                            }
                        }

                    }
                }
                LivingMob.getEquipment().clear();
            });
        } else {
            LivingMob.setMetadata("PLOOT", new FixedMetadataValue(realmcraft.getInstance(), ploot));
            LivingMob.setMetadata("TYPE", new FixedMetadataValue(realmcraft.getInstance(), type));
            LivingMob.setMetadata("CHANCE", new FixedMetadataValue(realmcraft.getInstance(), chance));
            LivingMob.setMetadata("LOCKPLAYER",
                    new FixedMetadataValue(realmcraft.getInstance(), lockPlayer.toString()));
            LivingMob.setMetadata("TICKSLOCKED", new FixedMetadataValue(realmcraft.getInstance(), ticksLocked));
            LivingMob.setMetadata("SPREAD", new FixedMetadataValue(realmcraft.getInstance(), spread.toString()));
        }
        return SkillResult.SUCCESS;
    }

    public String calculateLootBoxTier(double weight) {
        double x = weight / 4;
        double log2x = Math.log(x) / Math.log(2);
        double floorLog2x = Math.floor(log2x);
        double fracLog2x = log2x - floorLog2x;
        double pow2fracLog2x = Math.pow(2, fracLog2x);
        double ceilPow2fracLog2x = Math.ceil(pow2fracLog2x);
        double tierWeight = floorLog2x + ceilPow2fracLog2x / 2.0;
        if (tierWeight >= 8) {
            return "MYTHICAL";
        } else if (tierWeight >= 7) {
            return "UNIQUE";
        } else if (tierWeight >= 6) {
            return "LEGENDARY";
        } else if (tierWeight >= 5) {
            return "VERY_RARE";
        } else if (tierWeight >= 4) {
            return "EPIC";
        } else if (tierWeight >= 3) {
            return "RARE";
        } else if (tierWeight >= 2) {
            return "UNCOMMON";
        } else {
            return "COMMON";
        }
    }

}
