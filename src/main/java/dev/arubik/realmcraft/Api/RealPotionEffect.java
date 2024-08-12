package dev.arubik.realmcraft.Api;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class RealPotionEffect {
    public PotionEffectType type;
    public int level = 1;
    public int duration = 20;

    public RealPotionEffect() {
    }

    public RealPotionEffect(PotionEffectType type, int level, int duration) {
        this.type = type;
        this.level = level;
        this.duration = duration;
    }

    public void apply(@NotNull LivingEntity entity) {
        entity.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, level));
    }
}
