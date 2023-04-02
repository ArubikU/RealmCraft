package dev.arubik.realmcraft.Api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EmptyDynamicLine implements DynamicLoreLine {

    @Override
    public String parseLine(Player player, ItemStack item) {
        return "";
    }

}
