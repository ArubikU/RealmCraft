package dev.arubik.realmcraft.Storage;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.LorePosition;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Events.LoreEvent;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.types.item.config.ItemStorageConfig;
import me.clip.placeholderapi.PlaceholderAPI;

public class StorageLore implements ItemBuildModifier {

    private static String line1 = "<gray><!italic>Contenido: [{used_slots}/{max_slots}]";
    private static String line2 = "<gray>  <white><!italic>{item} x{amount}";
    private static String endline = "<gray>  <gray><!italic>Y mas...";
    private static String empty = "<gray>  <white><!italic>Vacio";

    private static StorageMechanic core = StorageMechanic.getInstance();

    @Override
    public Boolean able(RealNBT nbt) {

        // return
        // core.getManagers().getItemStorageManager().isItemStorage(nbt.getItemStack());
        return core.getManagers().getItemStorageConfigManager()
                .findItemStorageConfigByItemID(Adapter.getInstance().getAdapterID(nbt.getItemStack())) != null;// ||
                                                                                                               // VanillaWildtags.SHULKER_BOXES.validate(nbt.getItemStack());
    }

    public static Boolean genAble(RealNBT nbt) {

        // return
        // core.getManagers().getItemStorageManager().isItemStorage(nbt.getItemStack());
        if (nbt.getItemStack() == null)
            return false;
        if (nbt.getItemStack().getItemMeta() == null)
            return false;

        return core.getManagers().getItemStorageConfigManager()
                .findItemStorageConfigByItemID(Adapter.getInstance().getAdapterID(nbt.getItemStack())) != null;// ||
                                                                                                               // VanillaWildtags.SHULKER_BOXES.validate(nbt.getItemStack());
    }

    @Override
    public RealNBT modifyItem(Player player, RealNBT item) {

        // if (VanillaWildtags.SHULKER_BOXES.validate(item.getItemStack())) {
        //
        // }

        if (!core.getManagers().getItemStorageManager().isItemStorage(item.getItemStack())) {
            ItemStorageConfig config = core.getManagers().getItemStorageConfigManager()
                    .findItemStorageConfigByItemID(Adapter.getInstance().getAdapterID(item.getItemStack()));

            Storage storage = core.getManagers().getStorageManager().createStorage(config.getStorageConfigID(),
                    new StorageOriginContext(StorageOriginContext.Context.ITEM_STORAGE, new ArrayList<>() {
                        {
                            add(config.getId());
                            add(player.getUniqueId().toString());
                        }
                    }));
            List<String> lore = new ArrayList<>();
            lore.add(line1.replace("{used_slots}", storage.getOccupiedSlotsCount() + "").replace("{max_slots}",
                    storage.getTotalSlots() + ""));
            lore.add(empty);

            item.putLoreLines(lore, LorePosition.BOTTOM);
            return item;
        }
        String data = core.getManagers().getItemStorageManager()
                .getDataFromItemStack(item.getItemStack());
        String[] src = data.split(":");
        Storage storage = core.getManagers().getStorageManager().getStorage(src[1]);
        if (storage == null) {

        }

        List<String> lore = new ArrayList<>();
        lore.add(line1.replace("{used_slots}", storage.getOccupiedSlotsCount() + "").replace("{max_slots}",
                storage.getTotalSlots() + ""));
        if (storage.getOccupiedSlotsCount() == 0) {
            lore.add(empty);
        } else {
            for (int i = 0; i < storage.getAllItems().size(); i++) {
                if (i == 3) {
                    lore.add(endline);
                    break;
                }
                String itemLine = line2
                        .replace("{amount}", storage.getAllItems().get(i).getItemStack().getAmount() + "");
                RealNBT itemNBT = RealNBT.fromItemStack(storage.getAllItems().get(i).getItemStack());
                if (itemNBT.hasDisplayName()) {
                    itemLine = itemLine.replace("{item}", itemNBT.getDisplayName());
                } else {
                    itemLine = itemLine.replace("{item}",
                            PlaceholderAPI.setPlaceholders(player, itemNBT.getPlaceholderName()));
                }
                lore.add(itemLine);
            }
        }

        item.putLoreLines(lore, LorePosition.BOTTOM);
        return item;
    }

    public static void register() {
        LoreEvent.addItemBuildModifier(new StorageLore());
    }

    public static List<String> getItemsOfBackpack(RealNBT item) {
        // return the names and amounts of the items in the backpack
        // example: [ "item1 x1", "item2 x2", "item3 x3" ]
        // plain text, no colors
        List<String> items = new ArrayList<>();
        if (!genAble(item)) {
            return items;
        }
        if (StorageMechanic.getInstance().getManagers().getItemStorageManager().isItemStorage(item.getItemStack())) {
            String data = StorageMechanic.getInstance().getManagers().getItemStorageManager()
                    .getDataFromItemStack(item.getItemStack());
            String[] src = data.split(":");
            Storage storage = StorageMechanic.getInstance().getManagers().getStorageManager().getStorage(src[1]);
            if (storage == null) {
                return items;
            }
            for (int i = 0; i < storage.getAllItems().size(); i++) {
                if (storage.getAllItems().get(i).getItemStack().getItemMeta() == null)
                    continue;
                RealNBT nbt = RealNBT.fromItemStack(storage.getAllItems().get(i).getItemStack());
                items.add(storage.getAllItems().get(i).getItemStack().getAmount() + "x "
                        + nbt.getSimpleName() + " [" + nbt.dump() + "]  \n");
            }
        } else {
            ItemStorageConfig config = StorageMechanic.getInstance().getManagers().getItemStorageConfigManager()
                    .findItemStorageConfigByItemID(Adapter.getInstance().getAdapterID(item.getItemStack()));
            Storage storage = StorageMechanic.getInstance().getManagers().getStorageManager().createStorage(
                    config.getStorageConfigID(),
                    new StorageOriginContext(StorageOriginContext.Context.ITEM_STORAGE, new ArrayList<>() {
                        {
                            add(config.getId());
                            add("null");
                        }
                    }));
            for (int i = 0; i < storage.getAllItems().size(); i++) {
                if (storage.getAllItems().get(i).getItemStack().getItemMeta() == null)
                    continue;
                RealNBT nbt = RealNBT.fromItemStack(storage.getAllItems().get(i).getItemStack());
                items.add(storage.getAllItems().get(i).getItemStack().getAmount() + "x "
                        + nbt.getSimpleName()
                        + " [" + nbt.dump() + "]  \n");
            }
        }
        return items;
    }
}
