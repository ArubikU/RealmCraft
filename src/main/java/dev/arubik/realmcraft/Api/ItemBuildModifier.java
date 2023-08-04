package dev.arubik.realmcraft.Api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.LoreParser.ContextEvent;

public interface ItemBuildModifier {
    RealNBT modifyItem(Player player, RealNBT item);

    default Boolean able(RealNBT item) {
        return true;
    };

    default Boolean able(Player player, RealNBT item) {
        return true;
    };

    default Boolean able(RealNBT item, ContextEvent event) {
        return true;
    };
}
