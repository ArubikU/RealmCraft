package dev.arubik.realmcraft.MythicMobs;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.api.StorageMechanicAPI;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageItemDataInfo;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythiccrucible.MythicCrucible;
import io.lumine.mythiccrucible.items.furniture.Furniture;

public class MoveToNearestContainer implements ITargetedEntitySkill {

    public static StorageMechanic smapi = StorageMechanic.getInstance();

    public enum ContainerType {

        CHEST,
        DISPENSER,
        DROPPER,
        HOPPER,
        BARREL,
        SHULKER_BOX,
        ALL,
        FURNITURE
    }

    public enum ContainerAction {
        DEPOSIT,
        WITHDRAW
    }

    public enum Side {
        UP,
        DOWN,
        NORTH,
        SOUTH,
        EAST,
        WEST,
        RANDOM,
        ALL;

        public static Side randomRNG() {
            return values()[Utils.random(0, 5)];
        }
    }

    Set<ContainerType> containerType;
    ContainerAction containerAction;
    Side side;
    int stackSize;
    Boolean needViewHopper = false;

    public MoveToNearestContainer(MythicLineConfig config) {
        containerType = Set.of(List.of(config.getString("containerType", "ALL").split(",")).stream()
                .map(ContainerType::valueOf).toArray(ContainerType[]::new));
        containerAction = ContainerAction.valueOf(config.getString("containerAction", "DEPOSIT"));
        side = Side.valueOf(config.getString("side", "DOWN"));
        stackSize = config.getInteger("stackSize", 64);
        needViewHopper = config.getBoolean("needViewHopper", false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity arg1) {
        Bukkit.getScheduler().runTask(realmcraft.getInstance(), () -> {
            try {
                SkillCaster caster = skillMetadata.getCaster();
                String id = "";
                Location location = null;
                String idTriggerSkill = null;
                if (Compatibilities.isMythicCrucibleLoaded()) {
                    if (caster instanceof Furniture) {
                        location = BukkitAdapter.adapt(((Furniture) caster).getLocation());
                        id = ((Furniture) caster).getEntity().getUniqueId().toString();
                        idTriggerSkill = ((Furniture) caster).getFurnitureData().getItem().getInternalName();
                    }
                }
                if (caster instanceof ActiveMob) {
                    location = BukkitAdapter.adapt(((ActiveMob) caster).getLocation());
                    id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
                    idTriggerSkill = ((ActiveMob) caster).getType().getInternalName();
                }

                if (!smapi.getManagers().getStorageManager().storageExists(id))
                    return;
                Storage storage = smapi.getManagers().getStorageManager().getStorage(id);

                Block block = BukkitAdapter.adapt(arg1.getLocation()).getBlock();

                if (side != Side.ALL) {
                    if (side == Side.RANDOM) {
                        side = Side.randomRNG();
                    }
                    Block targetBlock = Utils.getRelativeBlock(block, side);

                    if (containerAction == ContainerAction.DEPOSIT) {
                        switch (targetBlock.getType()) {
                            case CHEST:
                            case DISPENSER:
                            case DROPPER:
                            case HOPPER:
                            case BARREL:
                            case SHULKER_BOX: {
                                if (containerType.contains(ContainerType.ALL)
                                        || containerType
                                                .contains(ContainerType.valueOf(targetBlock.getType().name()))) {
                                    Container chest = (Container) targetBlock.getState();
                                    if (targetBlock instanceof Hopper hopper) {
                                        if (hopper.isLocked())
                                            return;
                                    }
                                    if (needViewHopper) {
                                        if (targetBlock instanceof Hopper hopper) {
                                            // verify if the hopper is down
                                            if (hopper.getInventory().getLocation().getBlockY() < block.getY()) {
                                                return;
                                            }
                                        }
                                    }

                                    if (chest.getInventory().firstEmpty() == -1) {
                                        for (int i = 0; i < chest.getInventory().getSize(); i++) {
                                            ItemStack hopperItemStack = chest.getInventory().getItem(i);
                                            if (hopperItemStack == null)
                                                continue; // Agregado para evitar Null

                                            int hopperItemMaxStack = hopperItemStack.getMaxStackSize();
                                            int hopperItemAmount = hopperItemStack.getAmount();
                                            if (hopperItemAmount >= hopperItemMaxStack)
                                                continue; // Slot está completamente lleno

                                            StorageItemDataInfo similar = storage
                                                    .getFirstItemStackSimilar(hopperItemStack); // Obtener
                                                                                                // item
                                                                                                // similar
                                            if (similar == null)
                                                continue; // No hay item similar

                                            int amountSimilar = similar.getItemStack().getAmount();
                                            int spaceAvailableInHopper = hopperItemMaxStack - hopperItemAmount;

                                            // Calcula la cantidad real a transferir
                                            int actualstackSize = Math.min(Math.min(stackSize, spaceAvailableInHopper),
                                                    amountSimilar);

                                            if (actualstackSize > 0) {
                                                hopperItemStack.setAmount(hopperItemAmount + actualstackSize);
                                                similar.getItemStack().setAmount(amountSimilar - actualstackSize);

                                                if (similar.getItemStack().getAmount() == 0) {
                                                    similar.removeWithRestrictions();
                                                }

                                                // return SkillResult.SUCCESS; // Se ha transferido un item, devolver
                                                // SkillResult.SUCCES
                                            }
                                        }
                                    }

                                    else {
                                        StorageItemDataInfo storageItem = storage.getFirstItemStack();
                                        if (storageItem != null) {
                                            ItemStack storageStack = storageItem.getItemStack();
                                            int transferableAmount = Math.min(stackSize, storageStack.getAmount());

                                            ItemStack cloned = storageStack.clone();
                                            cloned.setAmount(transferableAmount);
                                            // Intenta agregar al hopper
                                            HashMap<Integer, ItemStack> notFit = chest.getInventory().addItem(cloned);

                                            // Si todos los ítems fueron añadidos exitosamente al hopper
                                            if (notFit.isEmpty()) {
                                                int newStorageAmount = storageStack.getAmount() - transferableAmount;
                                                if (newStorageAmount == 0)
                                                    storageItem.removeWithRestrictions();
                                                else
                                                    storageStack.setAmount(newStorageAmount);
                                                // return SkillResult.SUCCESS; // Retorna SkillResult.SUCCES después de
                                                // transferir el
                                                // primer ItemStack
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                            case BARRIER: {

                                // if (containerType == ContainerType.FURNITURE) {
                                // Location BlockCenter = targetBlock.getLocation().add(0.5, 0.5, 0.5);
                                // Entity furniture = null;
                                // Collection<ItemFrame> itemFrames =
                                // BlockCenter.getNearbyEntitiesByType(ItemFrame.class,
                                // 0.55);
                                // for (Entity e : itemFrames) {
                                // if (smapi.getManagers().getStorageManager()
                                // .storageExists(e.getUniqueId().toString())) {
                                // furniture = e;
                                // break;
                                // }
                                // }
                                // if (furniture == null)
                                // return;
                                // Storage storageFurniture = smapi.getManagers().getStorageManager()
                                // .getStorage(furniture.getUniqueId().toString());
                                //
                                // }
                                break;
                            }
                            default:
                                break;
                        }
                    } else {
                        switch (targetBlock.getType()) {
                            case FURNACE:
                            case CHEST:
                            case DISPENSER:
                            case DROPPER:
                            case HOPPER:
                            case BARREL:
                            case SHULKER_BOX:
                                if (containerType.contains(ContainerType.ALL)
                                        || containerType
                                                .contains(ContainerType.valueOf(targetBlock.getType().name()))) {
                                    Container chest = (Container) targetBlock.getState();
                                    if (targetBlock instanceof Hopper hopper) {
                                        if (hopper.isLocked())
                                            return;
                                    }
                                    if (needViewHopper) {
                                        if (targetBlock instanceof Hopper hopper) {
                                            Side FacingOfHopper = Utils.getFacingOfHopper(targetBlock);
                                            switch (FacingOfHopper) {
                                                case DOWN:
                                                    if (side != Side.UP)
                                                        return;
                                                    break;
                                                case UP:
                                                    if (side != Side.DOWN)
                                                        return;
                                                    break;
                                                case NORTH:
                                                    if (side != Side.SOUTH)
                                                        return;
                                                    break;
                                                case SOUTH:
                                                    if (side != Side.NORTH)
                                                        return;
                                                    break;
                                                case EAST:
                                                    if (side != Side.WEST)
                                                        return;
                                                    break;
                                                case WEST:
                                                    if (side != Side.EAST)
                                                        return;
                                                    break;
                                                default:
                                                    break;

                                            }
                                        }
                                    }

                                    for (int i = 0; i < chest.getInventory().getSize(); i++) {

                                        ItemStack itemHopper = chest.getInventory().getItem(i);

                                        if (itemHopper == null || itemHopper.getType().equals(Material.AIR))
                                            continue;

                                        int itemHopperAmount = itemHopper.getAmount();

                                        int amountToTransfer = Math.min(stackSize, itemHopperAmount);

                                        ItemStack itemTransfer = itemHopper.clone();
                                        itemTransfer.setAmount(amountToTransfer);
                                        List<ItemStack> list = storage
                                                .addItemStackToAllPagesWithRestrictions(itemTransfer);

                                        if (list.size() == 0) {
                                            itemHopper.setAmount(itemHopperAmount - stackSize);
                                            // return SkillResult.SUCCESS;
                                        }

                                        ItemStack itemStackReturned = list.get(0);

                                        if (itemStackReturned.getAmount() == amountToTransfer) {
                                            itemHopper.setAmount(itemHopperAmount);
                                            continue;
                                        }

                                        itemHopper.setAmount(itemHopperAmount - stackSize);

                                        itemHopper.setAmount(itemHopper.getAmount() + itemStackReturned.getAmount());

                                        // return SkillResult.SUCCESS;

                                    }

                                }

                            default: {

                            }
                        }

                    }

                } else {
                    for (Side s : Side.values()) {
                        if (s != Side.ALL && s != Side.RANDOM) {
                            side = s;
                            Block targetBlock = Utils.getRelativeBlock(block, s);

                            // under that some copy past code

                            if (containerAction == ContainerAction.DEPOSIT) {
                                switch (targetBlock.getType()) {
                                    case CHEST:
                                    case DISPENSER:
                                    case DROPPER:
                                    case HOPPER:
                                    case BARREL:
                                    case SHULKER_BOX: {
                                        if (containerType.contains(ContainerType.ALL)
                                                || containerType
                                                        .contains(
                                                                ContainerType.valueOf(targetBlock.getType().name()))) {
                                            Container chest = (Container) targetBlock.getState();
                                            if (targetBlock instanceof Hopper hopper) {
                                                if (hopper.isLocked())
                                                    return;
                                            }
                                            if (needViewHopper) {
                                                if (targetBlock instanceof Hopper hopper) {
                                                    // verify if the hopper is down
                                                    if (hopper.getInventory().getLocation().getBlockY() < block
                                                            .getY()) {
                                                        return;
                                                    }
                                                }
                                            }

                                            if (chest.getInventory().firstEmpty() == -1) {
                                                for (int i = 0; i < chest.getInventory().getSize(); i++) {
                                                    ItemStack hopperItemStack = chest.getInventory().getItem(i);
                                                    if (hopperItemStack == null)
                                                        continue; // Agregado para evitar Null

                                                    int hopperItemMaxStack = hopperItemStack.getMaxStackSize();
                                                    int hopperItemAmount = hopperItemStack.getAmount();
                                                    if (hopperItemAmount >= hopperItemMaxStack)
                                                        continue; // Slot está completamente lleno

                                                    StorageItemDataInfo similar = storage
                                                            .getFirstItemStackSimilar(hopperItemStack); // Obtener
                                                                                                        // item
                                                                                                        // similar
                                                    if (similar == null)
                                                        continue; // No hay item similar

                                                    int amountSimilar = similar.getItemStack().getAmount();
                                                    int spaceAvailableInHopper = hopperItemMaxStack - hopperItemAmount;

                                                    // Calcula la cantidad real a transferir
                                                    int actualstackSize = Math.min(
                                                            Math.min(stackSize, spaceAvailableInHopper),
                                                            amountSimilar);

                                                    if (actualstackSize > 0) {
                                                        hopperItemStack.setAmount(hopperItemAmount + actualstackSize);
                                                        similar.getItemStack()
                                                                .setAmount(amountSimilar - actualstackSize);

                                                        if (similar.getItemStack().getAmount() == 0) {
                                                            similar.removeWithRestrictions();
                                                        }

                                                        // return SkillResult.SUCCESS; // Se ha transferido un item,
                                                        // devolver
                                                        // SkillResult.SUCCES
                                                    }
                                                }
                                            }

                                            else {
                                                StorageItemDataInfo storageItem = storage.getFirstItemStack();
                                                if (storageItem != null) {
                                                    ItemStack storageStack = storageItem.getItemStack();
                                                    int transferableAmount = Math.min(stackSize,
                                                            storageStack.getAmount());

                                                    ItemStack cloned = storageStack.clone();
                                                    cloned.setAmount(transferableAmount);
                                                    // Intenta agregar al hopper
                                                    HashMap<Integer, ItemStack> notFit = chest.getInventory()
                                                            .addItem(cloned);

                                                    // Si todos los ítems fueron añadidos exitosamente al hopper
                                                    if (notFit.isEmpty()) {
                                                        int newStorageAmount = storageStack.getAmount()
                                                                - transferableAmount;
                                                        if (newStorageAmount == 0)
                                                            storageItem.removeWithRestrictions();
                                                        else
                                                            storageStack.setAmount(newStorageAmount);
                                                        // return SkillResult.SUCCESS; // Retorna SkillResult.SUCCES
                                                        // después
                                                        // de
                                                        // transferir el
                                                        // primer ItemStack
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    }
                                    case BARRIER: {

                                        // if (containerType == ContainerType.FURNITURE) {
                                        // Location BlockCenter = targetBlock.getLocation().add(0.5, 0.5, 0.5);
                                        // Entity furniture = null;
                                        // Collection<ItemFrame> itemFrames =
                                        // BlockCenter.getNearbyEntitiesByType(ItemFrame.class,
                                        // 0.55);
                                        // for (Entity e : itemFrames) {
                                        // if (smapi.getManagers().getStorageManager()
                                        // .storageExists(e.getUniqueId().toString())) {
                                        // furniture = e;
                                        // break;
                                        // }
                                        // }
                                        // if (furniture == null)
                                        // return;
                                        // Storage storageFurniture = smapi.getManagers().getStorageManager()
                                        // .getStorage(furniture.getUniqueId().toString());
                                        //
                                        // }
                                        break;
                                    }
                                    default:
                                        break;
                                }
                            } else {
                                switch (targetBlock.getType()) {
                                    case FURNACE:
                                    case CHEST:
                                    case DISPENSER:
                                    case DROPPER:
                                    case HOPPER:
                                    case BARREL:
                                    case SHULKER_BOX:
                                        if (containerType.contains(ContainerType.ALL)
                                                || containerType
                                                        .contains(
                                                                ContainerType.valueOf(targetBlock.getType().name()))) {
                                            Container chest = (Container) targetBlock.getState();
                                            if (targetBlock instanceof Hopper hopper) {
                                                if (hopper.isLocked())
                                                    return;
                                            }
                                            if (needViewHopper) {
                                                if (targetBlock instanceof Hopper hopper) {
                                                    Side FacingOfHopper = Utils.getFacingOfHopper(targetBlock);
                                                    switch (FacingOfHopper) {
                                                        case DOWN:
                                                            if (side != Side.UP)
                                                                return;
                                                            break;
                                                        case UP:
                                                            if (side != Side.DOWN)
                                                                return;
                                                            break;
                                                        case NORTH:
                                                            if (side != Side.SOUTH)
                                                                return;
                                                            break;
                                                        case SOUTH:
                                                            if (side != Side.NORTH)
                                                                return;
                                                            break;
                                                        case EAST:
                                                            if (side != Side.WEST)
                                                                return;
                                                            break;
                                                        case WEST:
                                                            if (side != Side.EAST)
                                                                return;
                                                            break;
                                                        default:
                                                            break;

                                                    }
                                                }
                                            }

                                            for (int i = 0; i < chest.getInventory().getSize(); i++) {

                                                ItemStack itemHopper = chest.getInventory().getItem(i);

                                                if (itemHopper == null || itemHopper.getType().equals(Material.AIR))
                                                    continue;

                                                int itemHopperAmount = itemHopper.getAmount();

                                                int amountToTransfer = Math.min(stackSize, itemHopperAmount);

                                                ItemStack itemTransfer = itemHopper.clone();
                                                itemTransfer.setAmount(amountToTransfer);
                                                List<ItemStack> list = storage
                                                        .addItemStackToAllPagesWithRestrictions(itemTransfer);

                                                if (list.size() == 0) {
                                                    itemHopper.setAmount(itemHopperAmount - stackSize);
                                                    // return SkillResult.SUCCESS;
                                                }

                                                ItemStack itemStackReturned = list.get(0);

                                                if (itemStackReturned.getAmount() == amountToTransfer) {
                                                    itemHopper.setAmount(itemHopperAmount);
                                                    continue;
                                                }

                                                itemHopper.setAmount(itemHopperAmount - stackSize);

                                                itemHopper
                                                        .setAmount(
                                                                itemHopper.getAmount() + itemStackReturned.getAmount());

                                                // return SkillResult.SUCCESS;

                                            }

                                        }

                                    default: {

                                    }
                                }

                            }

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        return SkillResult.SUCCESS;
    }
}
