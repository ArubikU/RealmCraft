package dev.arubik.realmcraft.LootGen;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.metadata.FixedMetadataValue;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.Handlers.RealMessage;

public class ContainerApi {

    public static boolean saveContainer(ContainerInstance instance) {
        if (realmcraft.getContainerInstances() != null) {
            InteractiveFile file = realmcraft.getContainerInstances();
            List<String> containers = file.getStringList(instance.getContainerPack(), RealNBT.EmptyList());
            containers.add(instance.toString());
            file.set(instance.getContainerPack(), containers);
            file.save();
            return true;
        }
        return false;
    }

    public static ContainerInstance fromPlayerView(Player player) {
        Block block = player.getTargetBlockExact(5);
        if (block == null) {
            return null;
        }
        if (block.getType().isBlock() == false) {
            return null;
        }
        if (ContainerInstance.getTypeMap().containsKey(block.getType()) == false) {
            return null;
        }
        if (realmcraft.getContainerInstances() != null) {
            InteractiveFile file = realmcraft.getContainerInstances();
            for (String key : file.getKeys()) {
                List<String> containers = file.getStringList(key, RealNBT.EmptyList());
                for (int i = 0; i < containers.size(); i++) {
                    ContainerInstance instance = ContainerInstance.fromString(containers.get(i));
                    if (instance != null) {
                        if (instance.getX() == block.getX()
                                && instance.getY() == block.getY()
                                && instance.getZ() == block.getZ()
                                && instance.getWorld().equalsIgnoreCase(block.getWorld().getName())) {
                            return instance;
                        }
                    }
                }
            }
        }
        return new ContainerInstance(block.getLocation(), "temporal", "minecraft");
    }

    public static void removeContainer(ContainerInstance container) {
        if (realmcraft.getContainerInstances() != null) {
            InteractiveFile file = realmcraft.getContainerInstances();
            for (String key : file.getKeys()) {
                List<String> containers = file.getStringList(key, RealNBT.EmptyList());
                List<String> newContainers = RealNBT.EmptyList();
                for (int i = 0; i < containers.size(); i++) {
                    ContainerInstance instance = ContainerInstance.fromString(containers.get(i));
                    if (instance != null) {
                        if (instance.getX() == container.getX()
                                && instance.getY() == container.getY()
                                && instance.getZ() == container.getZ()
                                && instance.getWorld().equalsIgnoreCase(container.getWorld())) {
                            continue;
                        }
                        newContainers.add(instance.toString());
                    }
                }
                file.set(container.getContainerPack(), newContainers);
            }
            file.save();

        }

        // remove metadata keys
        // lootTable and ContainerPack
        container.getLocation().getBlock().removeMetadata("lootTable", realmcraft.getInstance());
        container.getLocation().getBlock().removeMetadata("ContainerPack", realmcraft.getInstance());
    }

    public static void refillContainer(ContainerInstance container) {
        if (LootTable.validLootTable(container.getLootTable()) == false) {
            RealMessage.alert("Invalid loot table: " + container.getLootTable());
            return;
        }
        LootTable table = new LootTable(container.getLootTable());

        if (table.willDisapear()) {
            container.getLocation().getBlock().setType(Material.AIR);
            return;
        }

        container.getLocation().getBlock().setType(container.getTypeMaterial());
        container.getLocation().getBlock().setBlockData(container.getBlockData());
        container.getLocation().getBlock().getState().update();

        Container containerBlock = (Container) container.getLocation().getBlock().getState();

        containerBlock.getInventory().setContents(table.genLoot(container.getType()));
        containerBlock.setMetadata("CONTAINER_LOOT",
                new FixedMetadataValue(realmcraft.getInstance(), container.getLootTable()));

        table.setNameToContainer(container.getLocation());
    }

    public static void refillContainer(String containerPack) {
        if (realmcraft.getContainerInstances() != null) {
            InteractiveFile file = realmcraft.getContainerInstances();
            List<String> containers = file.getStringList(containerPack, RealNBT.EmptyList());
            for (int i = 0; i < containers.size(); i++) {
                ContainerInstance instance = ContainerInstance.fromString(containers.get(i));
                if (instance != null) {
                    refillContainer(instance);
                }
            }
        }
    }

    @EventHandler
    public void onContainerOpen(org.bukkit.event.inventory.InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Container) {
            Container container = (Container) event.getInventory().getHolder();
            if (container.hasMetadata("CONTAINER_LOOT")) {
                String lootTable = container.getMetadata("CONTAINER_LOOT").get(0).asString();
                if (LootTable.validLootTable(lootTable) == false) {
                    RealMessage.alert("Invalid loot table: " + lootTable);
                    return;
                }
                LootTable table = new LootTable(lootTable);
                table.applySkills(container.getLocation(), event.getPlayer());

                // remove metadata
                container.removeMetadata("CONTAINER_LOOT", realmcraft.getInstance());
            }
        }
    }

    public static void refillAllContainers() {
        if (realmcraft.getContainerInstances() != null) {
            InteractiveFile file = realmcraft.getContainerInstances();
            List<String> containerPacks = List.copyOf(file.getKeys());
            for (int i = 0; i < containerPacks.size(); i++) {
                refillContainer(containerPacks.get(i));
            }
        }
    }

    public List<String> containerPacks() {
        if (realmcraft.getContainerInstances() != null) {
            InteractiveFile file = realmcraft.getContainerInstances();
            return List.copyOf(file.getKeys());
        }
        return RealNBT.EmptyList();
    }

}
