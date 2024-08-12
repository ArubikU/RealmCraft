package dev.arubik.realmcraft.Api;

import org.bukkit.entity.Player;

public class EmptyDynamicLine implements DynamicLoreLine {

    @Override
    public String parseLine(Player player, RealNBT item) {
        return "";
    }

}
