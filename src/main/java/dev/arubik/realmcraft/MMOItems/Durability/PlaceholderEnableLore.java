package dev.arubik.realmcraft.MMOItems.Durability;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Events.LoreEvent;

public class PlaceholderEnableLore implements ItemBuildModifier {

    @Override
    public ItemStack modifyItem(Player player, ItemStack item) {
        RealNBT nbt = new RealNBT(item);
        nbt.setPlaceholderApi(player);
        return item;
    }

    @Override
    public Boolean able(ItemStack item) {
        RealNBT nbt = new RealNBT(item);
        return nbt.contains("PLACEHOLDERS_ENABLED");
    }

    public static void register() {
        LoreEvent.addItemBuildModifier(new PlaceholderEnableLore());
    }
}
