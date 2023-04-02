package dev.arubik.realmcraft.Api;

import org.bukkit.potion.PotionEffectType;

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
}
