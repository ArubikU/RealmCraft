package dev.arubik.realmcraft.MythicMobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageItemDataInfo;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythiccrucible.items.furniture.Furniture;

public class DropFromContainer implements ITargetedLocationSkill {

    public static StorageMechanic smapi = StorageMechanic.getInstance();

    String item;
    int stackSize;

    public DropFromContainer(MythicLineConfig config) {
        item = config.getString("item");
        stackSize = config.getInteger("stack-size");
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata skillMetadata, AbstractLocation arg1) {
        Bukkit.getScheduler().runTask(realmcraft.getInstance(), () -> {
            int amount = stackSize;
            SkillCaster caster = skillMetadata.getCaster();
            String id = "";
            Location location = BukkitAdapter.adapt(arg1);
            String idTriggerSkill = null;
            if (Compatibilities.isMythicCrucibleLoaded()) {
                if (caster instanceof Furniture) {
                    // location = BukkitAdapter.adapt(((Furniture) caster).getLocation());
                    id = ((Furniture) caster).getEntity().getUniqueId().toString();
                    idTriggerSkill = ((Furniture) caster).getFurnitureData().getItem().getInternalName();
                }
            }
            if (caster instanceof ActiveMob) {
                // location = BukkitAdapter.adapt(((ActiveMob) caster).getLocation());
                id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
                idTriggerSkill = ((ActiveMob) caster).getType().getInternalName();
            }

            if (!smapi.getManagers().getStorageManager().storageExists(id))
                return;
            Storage storage = smapi.getManagers().getStorageManager().getStorage(id);
            if (storage == null)
                return;
            for (StorageItemDataInfo itemd : storage.getAllItems()) {
                if (amount <= 0)
                    return;
                ItemStack itemStack = itemd.getItemStack();
                String type = "MINECRAFT";
                if (item.equalsIgnoreCase("ANY")) {
                    if (itemStack.getAmount() > amount) {
                        itemStack.setAmount(itemStack.getAmount() - amount);
                        ItemStack itemStack2 = itemStack.clone();
                        itemStack2.setAmount(amount);
                        location.getWorld().dropItem(location, itemStack2);
                        amount = 0;
                    } else if (itemStack.getAmount() == amount) {
                        ItemStack itemStack2 = itemStack.clone();
                        itemStack2.setAmount(itemStack.getAmount());
                        location.getWorld().dropItem(location, itemStack2);
                        itemStack.setAmount(0);
                        amount = 0;
                    } else {
                        ItemStack itemStack2 = itemStack.clone();
                        itemStack2.setAmount(itemStack.getAmount());
                        location.getWorld().dropItem(location, itemStack2);
                        itemStack.setAmount(0);
                        amount -= itemStack2.getAmount();
                    }
                } else {
                    RealNBT nbt = new RealNBT(itemStack);
                    if (nbt.contains("MYTHIC_TYPE")) {
                        if (!item.startsWith("MYTHIC"))
                            continue;
                        type = "MYTHIC_TYPE";
                    }
                    if (nbt.contains("MMOITEMS_TYPE")) {
                        if (!item.startsWith("MMOITEMS"))
                            continue;
                        type = "MMOITEMS";
                    }
                    switch (type) {
                        case "MINECRAFT":
                            if (itemStack.getType().name().equalsIgnoreCase(item)) {
                                drop(location, itemStack, amount);
                            }
                            break;
                        case "MYTHIC_TYPE":
                            if (nbt.getString("MYTHIC_TYPE").equalsIgnoreCase(item.replace("MYTHIC_TYPE.", ""))) {
                                drop(location, itemStack, amount);
                            }
                            break;
                        case "MMOITEMS":
                            // MMOITEMS_TYPE and MMOITEMS_ITEM_ID
                            // item = MMOITEMS.{MMOITEMS_TYPE}.{MMOITEMS_ITEM_ID}
                            if (nbt.getString("MMOITEMS_TYPE").equalsIgnoreCase(item.split("\\.")[1])
                                    && nbt.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(item.split("\\.")[2])) {
                                drop(location, itemStack, amount);
                            }

                        default:
                            break;
                    }
                }
            }

        });
        return null;
    }

    public void drop(Location location, ItemStack itemStack, int amount) {

        if (itemStack.getAmount() > amount) {
            itemStack.setAmount(itemStack.getAmount() - amount);
            ItemStack itemStack2 = itemStack.clone();
            itemStack2.setAmount(amount);
            location.getWorld().dropItem(location, itemStack2);
            amount = 0;
        } else if (itemStack.getAmount() == amount) {
            ItemStack itemStack2 = itemStack.clone();
            itemStack2.setAmount(itemStack.getAmount());
            location.getWorld().dropItem(location, itemStack2);
            itemStack.setAmount(0);
            amount = 0;
        } else {
            ItemStack itemStack2 = itemStack.clone();
            itemStack2.setAmount(itemStack.getAmount());
            location.getWorld().dropItem(location, itemStack2);
            itemStack.setAmount(0);
            amount -= itemStack2.getAmount();
        }
    }

}
