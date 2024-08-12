package dev.arubik.realmcraft.MythicMobs;

import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.RealNBT;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageItemDataInfo;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythiccrucible.items.furniture.Furniture;

public class ContainerContains implements ICasterCondition {

    String item;

    public static StorageMechanic smapi = StorageMechanic.getInstance();
    int stackSize;

    public ContainerContains(MythicLineConfig config) {

        item = config.getString("item");
        stackSize = config.getInteger("stack-size");

    }

    @Override
    public boolean check(SkillCaster caster) {

        int amount = stackSize;
        String id = "";
        String idTriggerSkill = null;
        if (Compatibilities.isMythicCrucibleLoaded()) {
            if (caster instanceof Furniture) {
                id = ((Furniture) caster).getEntity().getUniqueId().toString();
                idTriggerSkill = ((Furniture) caster).getFurnitureData().getItem().getInternalName();
            }
        }
        if (caster instanceof ActiveMob) {
            id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
            idTriggerSkill = ((ActiveMob) caster).getType().getInternalName();
        }

        if (!smapi.getManagers().getStorageManager().storageExists(id))
            return false;
        Storage storage = smapi.getManagers().getStorageManager().getStorage(id);
        if (storage == null)
            return false;
        for (StorageItemDataInfo itemd : storage.getAllItems()) {
            if (amount <= 0)
                return true;
            ItemStack itemStack = itemd.getItemStack();
            String type = "MINECRAFT";
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
                        if (itemStack.getAmount() >= amount) {
                            return true;
                        }
                    }
                    break;
                case "MYTHIC_TYPE":
                    if (nbt.getString("MYTHIC_TYPE").equalsIgnoreCase(item.replace("MYTHIC_TYPE.", ""))) {
                        if (itemStack.getAmount() >= amount) {
                            return true;
                        }
                    }
                    break;
                case "MMOITEMS":
                    // MMOITEMS_TYPE and MMOITEMS_ITEM_ID
                    // item = MMOITEMS.{MMOITEMS_TYPE}.{MMOITEMS_ITEM_ID}
                    if (nbt.getString("MMOITEMS_TYPE").equalsIgnoreCase(item.split("\\.")[1])
                            && nbt.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(item.split("\\.")[2])) {
                        if (itemStack.getAmount() >= amount) {
                            return true;
                        }
                    }

                default:
                    break;
            }
        }

        return false;
    }

}
