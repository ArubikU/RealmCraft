package dev.arubik.realmcraft.Api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface DynamicLoreLine {
    public String parseLine(Player player, ItemStack item);
}
