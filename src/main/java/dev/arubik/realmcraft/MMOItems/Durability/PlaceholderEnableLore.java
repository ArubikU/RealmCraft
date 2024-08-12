package dev.arubik.realmcraft.MMOItems.Durability;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Events.LoreEvent;
import dev.arubik.realmcraft.Managers.Module;

public class PlaceholderEnableLore implements ItemBuildModifier, Module {

    @Override
    public RealNBT modifyItem(Player player, RealNBT nbt) {
        nbt.setPlaceholderApi(player);
        return nbt;
    }

    @Override
    public Boolean able(RealNBT nbt) {
        return nbt.contains("PLACEHOLDERS_ENABLED");
    }

    @Override
    public void register() {
        // TO-DO Disabled until we can find a efficient way to handle this
        // LoreEvent.addItemBuildModifier(this);
    }

    @Override
    public String configId() {
        return "placeholder-lore";
    }

    @Override
    public String displayName() {
        return "Placeholder Lore";
    }
}
