package dev.arubik.realmcraft.MythicMobs;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;

public class MythicListener implements Listener, Depend {
    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase("touchlevel")) {
            event.register(new ModifyLevel(event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("dropequipment")) {
            event.register(new DropEquipment(event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("loottableskill")) {
            event.register(new LootTableSkill(event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("mmodurability")) {
            event.register(new MMODurabilityDamage(event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("MoveToNearestContainer")) {
            event.register(new MoveToNearestContainer(event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("DropFromContainer")) {
            event.register(new DropFromContainer(event.getConfig()));
        }
    }

    @EventHandler
    public void onMythicConditionLoad(MythicConditionLoadEvent event) {
        if (event.getConditionName().equalsIgnoreCase("notfromspawner")) {
            event.register(new IsFromSpawner());
        }
        if (event.getConditionName().equalsIgnoreCase("fromspawner")) {
            event.register(new isNotFromSpawner());
        }
        if (event.getConditionName().equalsIgnoreCase("ContainerContains")) {
            event.register(new ContainerContains(event.getConfig()));
        }
    }

    @EventHandler
    public void onMythicTargetLoad(MythicTargeterLoadEvent event) {
        if (event.getTargeterName().equalsIgnoreCase("InteligentItemInRadius")
                || event.getTargeterName().equalsIgnoreCase("BIIR")) {
            event.register(new InteligentItemInRadius(event.getContainer().getManager(), event.getConfig()));
        }
        if (event.getTargeterName().equalsIgnoreCase("InteligentMobInRadius")
                || event.getTargeterName().equalsIgnoreCase("BMIR")) {
            event.register(new InteligentMobInRadius(event.getContainer().getManager(), event.getConfig()));
        }
        Bukkit.getScheduler().runTask(realmcraft.getInstance(), () -> {
            if (event.getTargeterName().equalsIgnoreCase("FilteredItemsInRadius")) {
                event.register(new FilteredItemsInRadius(event.getConfig()));
            }
        });
        // if (event.getTargeterName().equalsIgnoreCase("FilteredItemsInRadius")) {
        // event.register(new FilteredItemsInRadius(event.getConfig()));
        // }
    }

    @EventHandler
    public void onMobDead(org.bukkit.event.entity.EntityDeathEvent event) {
        if (ModifyLevel.maxTimes.containsKey(event.getEntity().getUniqueId().toString())) {
            ModifyLevel.maxTimes.remove(event.getEntity().getUniqueId().toString());
        }
        if (event.getEntity().hasMetadata("fromSpawner")) {
            // split in 4 the xp drop
            event.setDroppedExp(event.getDroppedExp() / 4);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(org.bukkit.event.entity.CreatureSpawnEvent event) {
        if (event.getSpawnReason() == org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER) {
            event.getEntity().setMetadata("fromSpawner",
                    new org.bukkit.metadata.FixedMetadataValue(realmcraft.getInstance(), true));
        }
    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MythicMobs" };
    }

    public static void register() {
        MythicListener ml = new MythicListener();
        if (!Depend.isPluginEnabled(ml)) {
            RealMessage.nonFound("MythicMobs is not installed, so MythicMobs mechanics will not work.");
            return;
        } else {
            RealMessage.Found("MythicMobs is installed, so MythicMobs mechanics will work.");
        }
        Bukkit.getServer().getPluginManager().registerEvents(ml, realmcraft.getInstance());
    }

}
