package dev.arubik.realmcraft.MMOItems.Range;

import java.util.ArrayList;

import org.apache.logging.log4j.spi.ExtendedLogger;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Range;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.MMOItems.ExtendedLore;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.core.players.PlayerData;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.MMOItemsAPI;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class RangeListener implements Listener, Depend {

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MMOItems" };
    }

    public static void register() {
        RangeListener listener = new RangeListener();
        if (!listener.isDependatsPluginsEnabled())
            return;
        RealMessage.Found("MMOItems found starting register of RangeListener");
        Bukkit.getPluginManager().registerEvents(listener, realmcraft.getInstance());
        MMOItems.plugin.getStats().register("RANGE_CUSTOM", new RangeStat());
        MMOItems.plugin.getStats().register("EXTENDED_LORE", new ExtendedLore());
    }

    public static String NBT_TAG = "MMOITEMS_RANGE";
    public static String NBT_TAG_CUSTOM = "MMOITEMS_RANGE_CUSTOM";

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerAttackEvent event) {
        Player player = event.getAttacker().getPlayer();
        if (player == null) {
            return;
        }
        RealNBT nbt = new RealNBT(player.getInventory().getItemInMainHand());

        if (!(nbt.hasTag(NBT_TAG) || nbt.hasTag(NBT_TAG_CUSTOM))) {
            return;
        }
        Double range = nbt.getDouble(NBT_TAG, nbt.getDouble(NBT_TAG_CUSTOM));
        if (range == null) {
            return;
        }

        LiveMMOItem item = new LiveMMOItem(player.getInventory().getItemInMainHand());
        RPGPlayer playeRr = net.Indyuce.mmoitems.api.player.PlayerData.get(player).getRPG();
        for (ItemStat stat : item.getStats()) {
            if (stat instanceof ItemRestriction) {
                ItemRestriction restriction = (ItemRestriction) stat;
                if (!restriction.canUse(playeRr, NBTItem.get(player.getInventory().getItemInMainHand()), true)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        // get distance between player and target
        Double distance = player.getLocation().distance(event.getAttack().getTarget().getLocation());
        // RealMessage.sendRaw("Distance in Event: " + distance);
        // RealMessage.sendRaw("Range in Event: " + range);
        //// damage
        // RealMessage.sendRaw("Damage in Event: " + event.getAttack().getDamage());
        if (distance > range) {
            event.setCancelled(true);
        } else if (range >= 3 && distance <= 3) {
            event.setCancelled(false);
        } else {
            EntityType type = event.getAttack().getTarget().getType();
            if (type == EntityType.ENDER_CRYSTAL) {
                EnderCrystal crystal = (EnderCrystal) event.getAttack().getTarget();
                crystal.setInvulnerable(false);
                event.getAttacker().getPlayer().damage(event.getAttack().getDamage().getDamage(),
                        event.getAttack().getTarget());
                return;
            }
            if (event.getAttack().getTarget().getHealth() -
                    event.getAttack().getDamage().getDamage() <= 0) {
                event.getAttack().getTarget().setHealth(0);
                event.getAttack().getTarget().playEffect(EntityEffect.DEATH);
                Bukkit.getPluginManager()
                        .callEvent(new PlayerKillEntityEvent(event.getAttack(), (LivingEntity) event.getEntity()));

                event.setCancelled(true);
                return;
            }
            event.getAttack().getTarget()
                    .setHealth(event.getAttack().getTarget().getHealth() -
                            event.getAttack().getDamage().getDamage());
            event.getAttack().getTarget().playEffect(EntityEffect.HURT);
            Double knockback = event.getData().getStatMap().getStat("KNOCKBACK");

            // verify if the target is a player
            if (event.getAttack().getTarget() instanceof Player) {
                @NotNull
                MMOPlayerData data = MMOPlayerData.get(player);
                Double knockbackResistance = data.getStatMap().getStat("KNOCKBACK_RESISTANCE");
                knockback = knockback * (1 - knockbackResistance / 100);
            } else {
                LivingEntity target = (LivingEntity) event.getAttack().getTarget();
                Double knockbackResistance = target
                        .getAttribute(org.bukkit.attribute.Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue();
                knockback = knockback * (1 - knockbackResistance / 100);
            }
            if (knockback > 0) {
                Vector direction = event.getAttack().getTarget().getLocation().toVector()
                        .subtract(player.getLocation().toVector()).normalize();
                event.getAttack().getTarget().setVelocity(direction.multiply(knockback));
            }
        }
    }

    @EventHandler
    public void onPlayerAnimateTwo(PlayerArmSwingEvent event) {
        Player player = event.getPlayer();
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        if (inv.getItemInMainHand() != null) {
            return;
        }
        if (inv.getItemInMainHand().getType().isAir()) {
            return;
        }
        RealNBT nbt = new RealNBT(inv.getItemInMainHand());
        if (!(nbt.hasTag(NBT_TAG) || nbt.hasTag(NBT_TAG_CUSTOM))) {
            return;
        }
        if (inv.getItemInMainHand().getType().toString().contains("BOW")) {
            return;
        }
        Double range = nbt.getDouble(NBT_TAG, nbt.getDouble(NBT_TAG_CUSTOM));
        if (range == null) {
            return;
        }
        ArrayList<Entity> entitylist = getEntitiesBetweenPlayerAndBlockLookingAt(player);
        if (entitylist.size() == 0) {
            return;
        }
        for (Entity result : entitylist) {
            if (result != null) {
                Double distance = player.getLocation().distance(result.getLocation());
                if (distance > range) {
                    continue;
                }
                if (distance == 0) {
                    continue;
                }
                if (distance <= 3) {
                    return;
                }
                if (result == player) {
                    continue;
                }
                if (!(result instanceof LivingEntity)) {
                    player.damage(1, result);
                    return;
                }

                @NotNull
                MMOPlayerData data = MMOPlayerData.get(player);
                Double damage = data.getStatMap().getStat("ATTACK_DAMAGE");
                if (player.getAttackCooldown() <= 0.9) {
                    damage = damage * 0.1;
                }

                EntityDamageByEntityEvent event2 = new EntityDamageByEntityEvent(player,
                        result,
                        EntityDamageByEntityEvent.DamageCause.ENTITY_ATTACK, damage);
                Bukkit.getPluginManager().callEvent(event2);
                return;
            } else {
                continue;
            }
        }
    }

    public static ArrayList<Entity> getEntitiesBetweenPlayerAndBlockLookingAt(Player player) {
        Location loc = player.getEyeLocation();

        ArrayList<Entity> entitylist = new ArrayList<Entity>();

        double px = loc.getX();
        double py = loc.getY();
        double pz = loc.getZ();

        double yaw = Math.toRadians(loc.getYaw() + 90);
        double pitch = Math.toRadians(loc.getPitch() + 90);

        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch) * Math.sin(yaw);
        double z = Math.cos(pitch);

        for (int i = 1; i <= 70; i++) {
            Location loc1 = new Location(player.getWorld(), px + i * x, py + i * z, pz + i * y);
            for (Entity e : loc1.getNearbyLivingEntities(3, 3)) {
                if (e.getBoundingBox().contains(new Vector(loc1.getX(), loc1.getY(), loc1.getZ()))) {
                    entitylist.add(e);
                }
            }
        }
        return entitylist;
    }
}
