package dev.arubik.realmcraft.MMOItems.Range;

import org.bukkit.Material;

import net.Indyuce.mmoitems.stat.type.DoubleStat;

public class StatusEffectResistance extends DoubleStat {

    public StatusEffectResistance() {
        super("STATUS_EFFECT_RESISTANCE", Material.SPLASH_POTION, "Status Effect Potion Resistance", new String[] {
                "The resistance to recive damage from potions", "1.0 corresponds to 100%, 0.7 to 70%..." });
    }

    @Override
    public double multiplyWhenDisplaying() {
        return 100;
    }

}
