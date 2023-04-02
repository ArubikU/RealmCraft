package dev.arubik.realmcraft.Api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.LoreParser.ContextEvent;

public interface RealLore {
    List<DynamicLoreLine> getLoreLines();

    LorePosition getLorePosition();

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
