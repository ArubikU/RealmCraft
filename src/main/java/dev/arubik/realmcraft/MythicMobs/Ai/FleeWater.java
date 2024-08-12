package dev.arubik.realmcraft.MythicMobs.Ai;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.utils.annotations.MythicAIGoal;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

@MythicAIGoal(name = "WaterAvoid", aliases = {}, version = "4.8", description = "AvoidGoingToWater")
@AiTag(id = "fleewater")
public class FleeWater implements Goal<Mob> {

    private double speed;
    @Getter
    private Mob entity;
    private double shelterX;
    private double shelterY;
    private double shelterZ;
    private Location shelter;
    private double dq = 0.5F;

    public FleeWater(Mob entity) {
        this.entity = entity;
        speed = entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).getValue();
    }

    public void start() {
        isPossibleShelter();
    }

    protected boolean isPossibleShelter() {
        Vector3d vector3d = this.findPossibleShelter();

        if (vector3d == null) {
            return false;
        } else {
            this.shelterX = vector3d.x;
            this.shelterY = vector3d.y;
            this.shelterZ = vector3d.z;

            this.shelter = new Location(this.getEntity().getLocation().getWorld(), this.shelterX, this.shelterY,
                    this.shelterZ);
            return true;
        }
    }

    public void tick() {
        if (this.shelter != null) {
            this.entity.getPathfinder().moveTo(shelter);
        }
    }

    public boolean shouldEnd() {
        return this.entity.getLocation().distanceSquared(shelter) <= this.dq;
    }

    public void end() {
        this.shelter = null;
    }

    @Nullable
    protected Vector3d findPossibleShelter() {
        Random random = new Random();
        Location blockpos = this.getEntity().getLocation().getBlock().getLocation();

        for (int i = 0; i < 10; ++i) {
            Location blockpos1 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);

            if (blockpos.getWorld().hasStorm()) {
                boolean canSeeSky = blockpos.getWorld().getHighestBlockYAt(blockpos1) == blockpos1.getBlockY();
                if (!canSeeSky) {
                    return new Vector3d(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
                }
            } else {
                return new Vector3d(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
            }
        }

        return null;
    }

    @Override
    public boolean shouldActivate() {

        // verify if server tick is multiple of 20 , Bukkit.getServer().getCurrentTick()
        if (Bukkit.getServer().getCurrentTick() % 20 != 0) {
            return false;
        }

        Location blockpos = this.getEntity().getLocation().getBlock().getLocation();

        if (blockpos.getBlock().isLiquid() && blockpos.getBlock().getType() == org.bukkit.Material.WATER) {
            return true;
        }
        if (blockpos.getWorld().hasStorm()) {
            return (double) blockpos.getWorld().getHighestBlockYAt(blockpos) <= blockpos.getY() + 1.0D;
        }
        return false;
    }

    @Override
    public @NotNull GoalKey getKey() {
        return GoalCustomRegister.getKey("fleewater", FleeWater.class);
    }

    @Override
    public @NotNull EnumSet getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }
}
