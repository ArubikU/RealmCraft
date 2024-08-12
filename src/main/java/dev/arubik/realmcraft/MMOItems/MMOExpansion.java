package dev.arubik.realmcraft.MMOItems;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.MMOItems.Range.StatusEffectResistance;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class MMOExpansion implements Depend, Listener {

  @Override
  public String[] getDependatsPlugins() {
    return new String[] { "MMOItems", "MythicLib" };
  }

  public static void Register() {

    MMOItems.plugin.getStats().register("STATUS_EFFECT_RESISTANCE", new StatusEffectResistance());

    Bukkit.getPluginManager().registerEvents(new MMOExpansion(), realmcraft.getInstance());
  }

  @EventHandler
  public void onDamaged(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (event.getCause() == EntityDamageEvent.DamageCause.POISON
          || event.getCause() == EntityDamageEvent.DamageCause.WITHER) {
        double resistance = PlayerData.get(player).getStats().getMap().getStat("STATUS_EFFECT_RESISTANCE");
        event.setDamage(event.getDamage() * (1 - resistance));
      }
    }
  }

  @EventHandler
  public void onDamagedElemental(EntityDamageEvent event) {

    if (realmcraft.getInteractiveConfig().has("override-element." + event.getCause().toString())) {
      String[] args = realmcraft.getInteractiveConfig()
          .getString("override-element." + event.getCause().toString()).split(":");
      Element element = MythicLib.inst().getElements().getOrNull(args[0]);
      if (element != null) {

      }
    }

  }
}
