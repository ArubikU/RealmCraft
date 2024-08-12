package dev.arubik.realmcraft.Api.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dev.arubik.realmcraft.realmcraft;

public class BaseListener implements Listener {

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!realmcraft.CRAFT_ENHANCED_ENABLED) {
      CraftingEnhance.register();
      realmcraft.CRAFT_ENHANCED_ENABLED = true;
    }
  }

  public static void register() {
    KnifeListener.register();
    Bukkit.getPluginManager().registerEvents(new BaseListener(), realmcraft.getInstance());
  }
}
