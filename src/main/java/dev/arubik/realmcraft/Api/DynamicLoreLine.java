package dev.arubik.realmcraft.Api;

import org.bukkit.entity.Player;

public interface DynamicLoreLine {
    public String parseLine(Player player, RealNBT item);
}
