package dev.arubik.realmcraft.Api.Listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.RealPlayer;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.particles.Particle;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.element.Element;

public class AttackListener implements Listener, Depend {
    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MythicMobs", "MythicLib" };
    }

    public static void register() {
        AttackListener ml = new AttackListener();
        if (!Depend.isPluginEnabled(ml)) {
            RealMessage.nonFound(
                    "MythicMobs or MythicLib is not installed, so MythicMobs elements Link will not work.");
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(ml, realmcraft.getInstance());
    }

    @EventHandler
    public void onAttack(PlayerAttackEvent event) {
        HashMap<Element, Double> elements = new HashMap<>();
        for (Element element : Element.values()) {
            elements.put(element, event.getAttack().getDamage().getDamage(element));
        }
        InteractiveFile config = realmcraft.getInteractiveConfig();
        Entity target = event.getAttack().getTarget();
        BukkitAPIHelper mobManager = MythicBukkit.inst().getAPIHelper();
        if (mobManager.isMythicMob(target)) {
            if (config.has(
                    "MythicMobs." + mobManager.getMythicMobInstance(target).getType().getInternalName() + ".element")) {
                String elem = config.getString("MythicMobs."
                        + mobManager.getMythicMobInstance(target).getType().getInternalName() + ".element");
                for (Element element : Element.values()) {
                    Double modifier = config
                            .getDouble("Element." + element.getName() + "." + Element.valueOf(elem).getName());
                    if (modifier != null) {
                        event.getAttack().getDamage().additiveModifier(modifier, element);
                    }
                }
            }
        }
    }


}
