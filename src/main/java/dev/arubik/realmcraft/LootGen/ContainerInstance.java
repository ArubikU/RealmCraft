package dev.arubik.realmcraft.LootGen;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.metadata.FixedMetadataValue;

import dev.arubik.realmcraft.realmcraft;
import lombok.Getter;
import lombok.Setter;

public class ContainerInstance {
    @Getter
    private int x;
    @Getter
    private int y;
    @Getter
    private int z;
    @Getter
    private String world;
    @Getter
    private InventoryType type;
    @Getter
    private BlockData blockData;
    @Getter
    @Setter
    private String lootTable;
    @Getter
    @Setter
    private String ContainerPack;
    @Getter
    private Material typeMaterial;

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public ContainerInstance(Location loc, String lootTable, String ContainerPack) {
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.world = loc.getWorld().getName();

        this.lootTable = lootTable;
        this.ContainerPack = ContainerPack;
        // verify if the block contains metadata
        if (loc.getBlock().hasMetadata("lootTable")) {
            this.lootTable = loc.getBlock().getMetadata("lootTable").get(0).asString();
        } else {
            this.lootTable = lootTable;
        }
        if (loc.getBlock().hasMetadata("ContainerPack")) {
            this.ContainerPack = loc.getBlock().getMetadata("ContainerPack").get(0).asString();
        } else {
            this.ContainerPack = ContainerPack;
        }

        if (loc.getBlock().getType().isBlock()) {
            this.blockData = loc.getBlock().getBlockData();
            this.type = typeMap.get(loc.getBlock().getType());
            this.typeMaterial = loc.getBlock().getType();

            // push lootTable and ContainerPack to block metadata
            loc.getBlock().setMetadata("lootTable", new FixedMetadataValue(realmcraft.getInstance(), lootTable));
            loc.getBlock().setMetadata("ContainerPack",
                    new FixedMetadataValue(realmcraft.getInstance(), ContainerPack));
        }
    }

    public String toString() {

        return "ContainerInstace [x=" + x + "; y=" + y + "; z=" + z + "; world=" + world + "; type=" + type.toString()
                + "; blockData=" + blockData.getAsString() + "; lootTable=" + lootTable + "; ContainerPack="
                + ContainerPack + "]";
    }

    public static ContainerInstance fromString(String str) {
        if (str.startsWith("ContainerInstace [") == false) {
            return null;
        }
        str = str.substring(18);
        // remove last ]
        str = str.substring(0, str.length() - 1);
        String[] parts = str.split("; ");
        int x = Integer.parseInt(parts[0].split("=", 2)[1]);
        int y = Integer.parseInt(parts[1].split("=", 2)[1]);
        int z = Integer.parseInt(parts[2].split("=", 2)[1]);
        String world = parts[3].split("=", 2)[1];
        InventoryType type = InventoryType.valueOf(parts[4].split("=", 2)[1]);
        BlockData blockData = Bukkit.createBlockData(parts[5].split("=", 2)[1]);
        String lootTable = parts[6].split("=", 2)[1];
        String ContainerPack = parts[7].split("=", 2)[1];

        ContainerInstance container = new ContainerInstance(new Location(Bukkit.getWorld(world), x, y, z), lootTable,
                ContainerPack);
        container.blockData = blockData;
        container.type = type;
        container.typeMaterial = blockData.getMaterial();
        return container;
    }

    @Getter
    private static Map<Material, InventoryType> typeMap = new HashMap<Material, InventoryType>() {
        {
            put(Material.CHEST, InventoryType.CHEST);
            put(Material.TRAPPED_CHEST, InventoryType.CHEST);
            put(Material.DROPPER, InventoryType.DROPPER);
            put(Material.DISPENSER, InventoryType.DISPENSER);
            put(Material.HOPPER, InventoryType.HOPPER);
            put(Material.FURNACE, InventoryType.FURNACE);
            put(Material.BLAST_FURNACE, InventoryType.BLAST_FURNACE);
            put(Material.SMOKER, InventoryType.SMOKER);
            put(Material.BARREL, InventoryType.BARREL);
            put(Material.BREWING_STAND, InventoryType.BREWING);
            put(Material.CRAFTING_TABLE, InventoryType.WORKBENCH);
            put(Material.ENCHANTING_TABLE, InventoryType.ENCHANTING);
            put(Material.ANVIL, InventoryType.ANVIL);
            put(Material.GRINDSTONE, InventoryType.GRINDSTONE);
            put(Material.STONECUTTER, InventoryType.STONECUTTER);
            put(Material.LECTERN, InventoryType.LECTERN);
            put(Material.SMITHING_TABLE, InventoryType.SMITHING);
            put(Material.LOOM, InventoryType.LOOM);
            put(Material.CARTOGRAPHY_TABLE, InventoryType.CARTOGRAPHY);
        }
    };

}
