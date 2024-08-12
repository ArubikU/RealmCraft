package dev.arubik.realmcraft.Api.Listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class DodgeListener implements Listener {

    public static Plugin plugin;

    public DodgeListener(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        DodgeListener.plugin = plugin;
    }

    public static void register(Plugin plugin) {
        new DodgeListener(plugin);
    }

    Map<UUID, Direction> lastDirection = new HashMap<UUID, Direction>();

    public enum Direction {
        NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Vector direction = event.getTo().subtract(event.getFrom()).toVector();
        if (direction.getX() > 0)
            direction.setX(1);
        if (direction.getX() < 0)
            direction.setX(-1);
        if (direction.getZ() > 0)
            direction.setZ(1);
        if (direction.getZ() < 0)
            direction.setZ(-1);

        Direction dir = Direction.NORTH;
        if (direction.getX() == 1 && direction.getZ() == 1)
            dir = Direction.NORTH_EAST;
        if (direction.getX() == 1 && direction.getZ() == 0)
            dir = Direction.EAST;
        if (direction.getX() == 1 && direction.getZ() == -1)
            dir = Direction.SOUTH_EAST;
        if (direction.getX() == 0 && direction.getZ() == -1)
            dir = Direction.SOUTH;
        if (direction.getX() == -1 && direction.getZ() == -1)
            dir = Direction.SOUTH_WEST;
        if (direction.getX() == -1 && direction.getZ() == 0)
            dir = Direction.WEST;
        if (direction.getX() == -1 && direction.getZ() == 1)
            dir = Direction.NORTH_WEST;
        lastDirection.put(event.getPlayer().getUniqueId(), dir);

    }

    @EventHandler
    public void onRightClick(org.bukkit.event.player.PlayerInteractEvent event) {
        // verify if if rightclick to air and m
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {

            // get hunger saturation
            float saturation = event.getPlayer().getSaturation();
            float food = event.getPlayer().getFoodLevel();

            float velocityMultiplier = 1;

            float dodgecost = 2.0f;
            if (saturation > dodgecost) {
                event.getPlayer().setSaturation(saturation - dodgecost);
                velocityMultiplier = 0.75f;
            } else {
                if (saturation + food > dodgecost) {
                    saturation = saturation - dodgecost;
                    food = food - (dodgecost - saturation);
                    event.getPlayer().setSaturation(0);
                    event.getPlayer().setFoodLevel((int) food);
                    velocityMultiplier = 1.0f;
                } else {
                    event.getPlayer().setSaturation(0);
                    event.getPlayer().setFoodLevel(0);
                    velocityMultiplier = 0.0f;
                }

            }
            if (velocityMultiplier == 0.0f) {
                // force a stop
                event.getPlayer().setVelocity(event.getPlayer().getVelocity().setX(0));
                event.getPlayer().setVelocity(event.getPlayer().getVelocity().setZ(0));
                // play minecraft:item.shield.break to indicate the player is out of stamina
                event.getPlayer().playSound(event.getPlayer().getLocation(), "minecraft:item.shield.break", 1.0f, 1.0f);
                return;
            }
            // apply a 0.5 jump up to the player
            event.getPlayer().setVelocity(event.getPlayer().getVelocity().setY(0.5));

            // execute a dash in the direction of the movement and apply particle effects
            event.getPlayer().setVelocity(
                    event.getPlayer().getVelocity()
                            .setX(event.getPlayer().getVelocity().getX() + 1 * velocityMultiplier));
            event.getPlayer().setVelocity(
                    event.getPlayer().getVelocity()
                            .setZ(event.getPlayer().getVelocity().getZ() + 1 * velocityMultiplier));
            // play 10 ticks of white particles under the feet of the player
            Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks > 10) {
                        return;
                    }
                    event.getPlayer().getWorld().spawnParticle(org.bukkit.Particle.SPELL_WITCH,
                            event.getPlayer().getLocation().add(0, 0.1, 0), 1, 0, 0, 0, 0);
                    ticks++;
                }
            }, 0, 1);

        }
    }

}
