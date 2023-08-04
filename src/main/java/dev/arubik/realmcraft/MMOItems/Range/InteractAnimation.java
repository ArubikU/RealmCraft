package dev.arubik.realmcraft.MMOItems.Range;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.BooleanStat;

public class InteractAnimation extends BooleanStat implements Listener, Depend {

    public InteractAnimation(String id, Material mat, String name, String[] lore, String[] types,
            Material[] materials) {
        super(id, mat, name, lore, types, materials);
    }

    public InteractAnimation() {
        super("INTERACTION_ANIM", Material.GOLDEN_SWORD, "Interaction Animation", new String[] {},
                new String[] { "all" });
    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MMOItems" };
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() == null)
                return;
            RealNBT nbt = new RealNBT(event.getItem());
            if (nbt.hasTag("MMOITEMS_INTERACTION_ANIM") || nbt.hasTag("INTERACTION_ANIM")) {
                event.getPlayer().swingMainHand();
            }
        }
    }

    public static void register() {
        InteractAnimation listener = new InteractAnimation();
        if (!listener.isDependatsPluginsEnabled())
            return;
        RealMessage.Found("MMOItems found starting register of Interaction Animation");
        Bukkit.getPluginManager().registerEvents(listener, realmcraft.getInstance());
        MMOItems.plugin.getStats().register("INTERACTION_ANIM", listener);
    }
}
