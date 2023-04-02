package dev.arubik.realmcraft.Api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.LoreParser.ContextEvent;

public interface ItemBuildModifier {
    ItemStack modifyItem(Player player, ItemStack item);

    default Boolean able(ItemStack item) {
        return true;
    };

    default Boolean able(Player player, ItemStack item) {
        return true;
    };

    default Boolean able(ItemStack item, ContextEvent event) {
        return true;
    };
}
