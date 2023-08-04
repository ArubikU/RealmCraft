package dev.arubik.realmcraft.MMOItems.Durability;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Events.LoreEvent;

public class PlaceholderEnableLore implements ItemBuildModifier {

    @Override
    public RealNBT modifyItem(Player player, RealNBT nbt) {
        nbt.setPlaceholderApi(player);
        return nbt;
    }

    @Override
    public Boolean able(RealNBT nbt) {
        return nbt.contains("PLACEHOLDERS_ENABLED");
    }

    public static void register() {
        LoreEvent.addItemBuildModifier(new PlaceholderEnableLore());
    }
}
