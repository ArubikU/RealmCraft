package dev.arubik.realmcraft.Api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public abstract class CooldownIntegration {

  public int getCooldownTime() {
    return 10;
  }

  Map<UUID, Long> cooldownMap = new HashMap<>();

  protected boolean CooldownAvaliable(Player player) {
    UUID playerId = player.getUniqueId();
    if (!cooldownMap.containsKey(playerId)) {
      return true;
    }
    long currentTime = System.currentTimeMillis();
    long lastCastTime = cooldownMap.get(playerId);
    return currentTime - lastCastTime >= getCooldownTime();
  }

  protected void CooldownStart(Player player) {
    UUID playerId = player.getUniqueId();
    long currentTime = System.currentTimeMillis();
    cooldownMap.put(playerId, currentTime);
  }
}
