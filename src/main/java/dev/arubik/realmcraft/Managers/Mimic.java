package dev.arubik.realmcraft.Managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;

public class Mimic implements Listener {

    String mob = "vp1_mimic_chest_awaken";
    Map<UUID, ItemStack[]> mobLoots = new HashMap<>();
    double chance = 5;
    boolean Enabled = true;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLootGen(LootGenerateEvent event) {
        if (!Enabled)
            return;

        if (event.getInventoryHolder() instanceof Container container) {

            // verify if is chest and not a double chest
            if (container.getBlock().getType() == org.bukkit.Material.CHEST) {
                Block theChestBlock = container.getBlock();
                BlockState chestState = theChestBlock.getState();
                if (chestState instanceof Chest) {
                    Chest chest = (Chest) chestState;
                    Inventory inventory = chest.getInventory();
                    if (inventory instanceof DoubleChestInventory) {
                        return;
                    }
                }

                if (Utils.Chance(chance, 100)) {

                    ItemStack[] loot = event.getLoot().toArray(ItemStack[]::new);
                    MythicBukkit plugin = MythicBukkit.inst();
                    UUID uuid;
                    if (plugin.getMobManager().getMythicMob(mob).isPresent()) {
                        MythicMob mythicMob = plugin.getMobManager().getMythicMob(mob).get();
                        AbstractLocation loc = BukkitAdapter.adapt(container.getLocation());
                        uuid = mythicMob.spawn(loc, 1).getUniqueId();
                    } else {
                        return;
                    }
                    mobLoots.put(uuid, loot);
                    container.getBlock().setType(org.bukkit.Material.AIR);

                    // close inv for nearby players
                    Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getLocation().distance(container.getLocation()) < 10) {
                                player.closeInventory();
                            }
                        }
                    }, 1L);
                }
            }

        }
    }

    @EventHandler
    public void onDeathEntity(org.bukkit.event.entity.EntityDeathEvent event) {
        if (!Enabled)
            return;

        if (mobLoots.containsKey(event.getEntity().getUniqueId())) {
            ItemStack[] loot = mobLoots.get(event.getEntity().getUniqueId());
            // drop loot
            for (ItemStack item : loot) {
                if (item != null) {
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), item);
                }
            }
            mobLoots.remove(event.getEntity().getUniqueId());
        }
    }

    public static void Setup() {
        Mimic mimic = new Mimic();
        mimic.Enabled = true;
        mimic.chance = 8;
        mimic.mob = "vp1_mimic_chest_awaken";
        Bukkit.getServer().getPluginManager().registerEvents(mimic, realmcraft.getInstance());
        RealMessage.sendConsoleMessage("Mimic Manager Loaded");
    }
}
