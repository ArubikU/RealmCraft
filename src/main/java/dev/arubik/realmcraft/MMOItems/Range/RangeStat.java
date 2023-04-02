package dev.arubik.realmcraft.MMOItems.Range;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Events.LoreEvent;
import net.Indyuce.mmoitems.stat.type.DoubleStat;

public class RangeStat extends DoubleStat {

    public RangeStat(String id, Material mat, String name, String[] lore, String[] types,
            Material[] materials) {
        super(id, mat, name, lore, types, materials);
    }

    public RangeStat() {
        super("RANGE_CUSTOM", Material.GOLDEN_SWORD, "Set the range of the melee weapon", new String[] {},
                new String[] { "all" });
    }
}
