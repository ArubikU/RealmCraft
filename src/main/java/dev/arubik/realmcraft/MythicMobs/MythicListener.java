package dev.arubik.realmcraft.MythicMobs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.comphenix.protocol.reflect.FieldUtils;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.drops.Drop;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;
import net.seyarada.pandeloot.Constants;
import net.seyarada.pandeloot.drops.ActiveDrop;
import net.seyarada.pandeloot.drops.IDrop;
import net.seyarada.pandeloot.drops.LootDrop;
import net.seyarada.pandeloot.drops.containers.ContainerManager;
import net.seyarada.pandeloot.drops.containers.LootBag;
import net.seyarada.pandeloot.flags.FlagPack;
import net.seyarada.pandeloot.flags.enums.FlagTrigger;
import net.seyarada.pandeloot.utils.ItemUtils;

public class MythicListener implements Listener, Depend {
    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase("touchlevel")) {
            if (!Depend.isPluginEnabled("MMOCore")) {
                RealMessage.nonFound("MMOCore is not installed, so ModifyLevel mechanic will not work.");
                return;
            }
            RealMessage.Found("MMOCore is installed, so ModifyLevel mechanic will work.");
            event.register(new ModifyLevel(event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("playeremote")) {
            if (!Depend.isPluginEnabled("AquaticModelEngine")) {
                RealMessage.nonFound("AquaticModelEngine is not installed, so playeremote mechanic will not work.");
                return;
            }
            RealMessage.Found("AquaticModelEngine is installed, so playeremote mechanic will work.");
            event.register(new PlayerEmote(event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("dropequipment")) {
            event.register(new DropEquipment(event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("loottableskill")) {
            event.register(new LootTableSkill(event.getConfig()));
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
