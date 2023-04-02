package dev.arubik.realmcraft.LootGen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealCache;
import dev.arubik.realmcraft.Api.RealMethods;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.RealStack;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveFolder;
import dev.arubik.realmcraft.FileManagement.InteractiveSection;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.SkillMetadataImpl;
import jline.internal.Nullable;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class LootTable {

    public static InteractiveFolder lootTable;

    public static List<InteractiveFile> lootTables;

    private RealCache<InteractiveSection> lootTableCache = new RealCache<InteractiveSection>();

    private List<InteractiveSection> Items = new ArrayList<InteractiveSection>();
    private Map<String, SkillMechanic> Skills = new HashMap<String, SkillMechanic>();
    private int OriginalItemsSize = 0;
    boolean continueProcess;

    public static void reload() {
        lootTable = new InteractiveFolder("lootTable", realmcraft.getInstance());
        lootTables = lootTable.getSubFiles();
        for (InteractiveFolder folder : lootTable.getSubFolders()) {
            for (InteractiveFile file : folder.getSubFiles()) {
                lootTables.add(file);
            }
        }
    }

    public boolean willDisapear() {
        return Utils.Chance(lootTableCache.forcedGet().getOrDefault("willDisapear", 0), 100);
    }

    public static Set<String> getLootTableNames() {
        Set<String> lootTableNames = new HashSet<String>();
        for (InteractiveFile file : lootTables) {
            lootTableNames.addAll(file.getKeys());
        }
        return lootTableNames;
    }

    public static Boolean validLootTable(String lootName) {
        for (InteractiveFile file : lootTables) {
            if (file.has(lootName)) {
                return true;
            }
        }
        return false;
    }

    public LootTable(String lootName) {
        continueProcess = false;
        for (InteractiveFile file : lootTables) {
            if (file.has(lootName)) {
                continueProcess = true;
                lootTableCache.set(file.getSection(lootName));
            }
        }

        if (lootTableCache.forcedGet().has("randomTable")) {
            List<String> randomTables = lootTableCache.forcedGet().getStringList("randomTable");
            String randomTable = Utils.randomFromList(randomTables);
            if (randomTable == null) {
                randomTable = Utils.randomFromList(randomTables);
            }
            if (randomTable == null) {
                randomTable = lootName;
            }
            if (randomTable.equalsIgnoreCase("self")) {
                randomTable = lootName;
            }
            for (InteractiveFile file : lootTables) {
                if (file.has(randomTable)) {
                    continueProcess = true;
                    lootTableCache.set(file.getSection(randomTable));
                }
            }

            if (lootTableCache.forcedGet().has("Skills")) {
                List<String> skills = lootTableCache.forcedGet().getStringList("Skills");
                for (String skill : skills) {
                    if (MythicBukkit.inst().getSkillManager().getMechanic(skill) != null) {
                        if (Skills.containsKey(skill))
                            return;
                        Skills.put(skill, MythicBukkit.inst().getSkillManager().getMechanic(skill));
                    }
                }
            }
        }

        if (continueProcess) {
            for (String key : lootTableCache.forcedGet().getSection("Items").getKeys()) {
                OriginalItemsSize += 1;
                InteractiveSection item = lootTableCache.forcedGet().getSection("Items").getSection(key);
                int weight = item.getOrDefault("Weight", 1);
                for (int i = 0; i < weight; i++) {
                    Items.add(item);
                }
            }
        }
    }

    @Nullable
    public LootTable ofName(String lootName) {
        return new LootTable(lootName);
    }

    public String getDisplayName() {
        if (!continueProcess) {
            RealMessage.alert("Dont exist file section");
            return null;
        }
        return lootTableCache.forcedGet().getOrDefault("DisplayName", "LootTable");
    }

    public void setNameToContainer(Location container) {
        if (!continueProcess) {
            RealMessage.alert("Dont exist file section");
            return;
        }
        Container c = (Container) container.getBlock().getState();
        Component name = MiniMessage.miniMessage().deserialize(getDisplayName());
        c.customName(name);
        c.update();
    }

    public static SkillTrigger ON_SPAWN_TRIGGER = SkillTrigger.create("onSpawnChest", "onSC");
    public static SkillTrigger ON_OPEN_TRIGGER = SkillTrigger.create("onOpenChest", "onPC");

    public void applySkills(Location container) {
        if (!continueProcess) {
            RealMessage.alert("Dont exist file section");
            return;
        }
        if (lootTableCache.forcedGet().has("Skills")) {
            AbstractLocation abs = BukkitAdapter.adapt(container);
            SkillMetadata data = new SkillMetadataImpl(LootTable.ON_SPAWN_TRIGGER, null, null, abs, null, null,
                    OriginalItemsSize);
            Skills.values().forEach(skill -> {
                skill.executeSkills(data);
            });
        }
    }

    public void applySkills(Location container, Entity player) {
        if (!continueProcess) {
            RealMessage.alert("Dont exist file section");
            return;
        }
        if (lootTableCache.forcedGet().has("Skills")) {
            AbstractLocation abs = BukkitAdapter.adapt(container);
            Collection<AbstractEntity> entities = new ArrayList<AbstractEntity>();
            entities.add(BukkitAdapter.adapt(player));
            SkillMetadata data = new SkillMetadataImpl(LootTable.ON_OPEN_TRIGGER, null, null, abs, entities, null,
                    OriginalItemsSize);
            Skills.values().forEach(skill -> {
                skill.executeSkills(data);
            });
        }
    }

    public ItemStack[] genLoot(InventoryType type, Player player) {
        ItemStack[] loot = new ItemStack[type.getDefaultSize()];
        if (!continueProcess) {
            RealMessage.alert("Dont exist file section");
            return loot;
        }
        int AirSlots = lootTableCache.forcedGet().getOrDefault("AirSlots", 0);
        int MaxItems = lootTableCache.forcedGet().getOrDefault("MaxItems", type.getDefaultSize());
        int MinItems = lootTableCache.forcedGet().getOrDefault("MinItems", OriginalItemsSize);

        int items = Utils.random(MinItems, MaxItems);
        Integer[] emptySlots = emptySlot.get(type).clone();
        // fill empty slot array
        // set random air slots RealNBT.empty() == air
        for (int i = 0; i < AirSlots; i++) {
            // random slot
            int slot = Utils.random(0, loot.length - 1);
            if (loot[slot] == null) {
                loot[slot] = RealNBT.Empty;
                // remove slot from emptySlots
                emptySlots = Utils.removeInt(emptySlots, slot);
            }
        }

        // get random items from list
        List<ItemStack> NewItems = new ArrayList<ItemStack>();
        List<String> usedItems = new ArrayList<String>();
        boolean isUnique = lootTableCache.forcedGet().getOrDefault("UniqueItems", false);
        for (int i = 0; i < items; i++) {
            InteractiveSection itemC = Items.get(Utils.random(0, Items.size() - 1));
            if (isUnique) {
                if (usedItems.contains(itemC.getPath())) {
                    if (usedItems.size() == Items.size())
                        break;
                    i--;
                    continue;
                }
                usedItems.add(itemC.getPath());
            }
            ItemStack a = RealStack.genLoot(itemC, player);

            if (a != null) {
                if (itemC.has("EnchantList") && Utils.Chance(itemC.getOrDefault("EnchantChance", 101), 100)) {

                    int maxEnchants = itemC.getOrDefault("MaxEnchants", 0);
                    int minEnchants = itemC.getOrDefault("MinEnchants", 0);
                    int enchants = Utils.random(minEnchants, maxEnchants);

                    List<String> EnchantList = itemC.getOrDefault("EnchantList", new ArrayList<String>());
                    List<String> UsedEnchants = new ArrayList<String>();
                    for (int j = 0; j < enchants; j++) {
                        String enchant = Utils.randomFromList(EnchantList);
                        if (enchant == null)
                            continue;
                        if (UsedEnchants.contains(enchant)) {
                            if (UsedEnchants.size() == EnchantList.size())
                                break;
                            j--;
                            UsedEnchants.add(enchant);
                            continue;
                        }
                        // enchant line have 2 parts, ENCHANT_NAME and LEVEL is a format of
                        // random(#anynumber#,#maxlevel#) or a number
                        String[] enchantParts = enchant.split(" ");

                        Enchantment e = Enchantment.getByName(enchantParts[0]);
                        String levelPart = enchantParts[1];
                        levelPart = levelPart.replace("#maxlevel#", String.valueOf(e.getMaxLevel()));
                        String[] levelparts = levelPart.split("to");
                        double levelD = Utils.random(Integer.parseInt(levelparts[0]), Integer.parseInt(levelparts[1]));

                        // remove enchant from list
                        EnchantList = Utils.removeString(EnchantList, enchant);
                        a.addUnsafeEnchantment(e, (int) levelD);
                    }

                }

                // read if item config has SpreadInInventory
                if (itemC.getOrDefault("SpreadInInventory", false)) {
                    // get value of spreadSize , that respond to the number of slots that the item
                    // will be spread
                    int spreadSize = itemC.getOrDefault("SpreadSize", 1);
                    // get rest of empty slots

                    if (spreadSize > a.getAmount())
                        spreadSize = a.getAmount();
                    int rest = items - i;
                    // if the number of slots that the item will be spread is greater than the rest
                    // of empty slots
                    if (spreadSize > rest) {
                        spreadSize = rest;
                    }
                    // create ItemStack copy and use a spread of 30 to 50% of original item amount
                    List<ItemStack> spreadItems = new ArrayList<ItemStack>();
                    int itemAmount = a.getAmount();
                    for (int j = 0; j < spreadSize; j++) {
                        ItemStack spreadItem = a.clone();
                        int am = Utils.random(itemAmount / 2, itemAmount * 2);
                        if (am > itemAmount)
                            am = itemAmount;
                        itemAmount -= am;
                        spreadItem.setAmount(am);
                        spreadItems.add(spreadItem);
                    }
                    // add spread items to NewItems
                    NewItems.addAll(spreadItems);
                } else {
                    NewItems.add(a);
                }

            }
        }

        // get random empty slots
        Integer[] slots = Utils.randomizeArrayOrder(emptySlots);
        // fill empty slots with items
        for (int i = 0; i < NewItems.size(); i++) {
            loot[slots[i]] = NewItems.get(i);
        }

        return loot;
    }

    public ItemStack[] genLoot(InventoryType type) {
        ItemStack[] loot = new ItemStack[type.getDefaultSize()];
        if (!continueProcess) {
            RealMessage.alert("Dont exist file section");
            return loot;
        }
        int AirSlots = lootTableCache.forcedGet().getOrDefault("AirSlots", 0);
        int MaxItems = lootTableCache.forcedGet().getOrDefault("MaxItems", type.getDefaultSize());
        int MinItems = lootTableCache.forcedGet().getOrDefault("MinItems", OriginalItemsSize);

        int items = Utils.random(MinItems, MaxItems);
        Integer[] emptySlots = emptySlot.get(type).clone();
        // fill empty slot array
        // set random air slots RealNBT.empty() == air
        for (int i = 0; i < AirSlots; i++) {
            // random slot
            int slot = Utils.random(0, loot.length - 1);
            if (loot[slot] == null) {
                loot[slot] = RealNBT.Empty;
                // remove slot from emptySlots
                emptySlots = Utils.removeInt(emptySlots, slot);
            }
        }

        // get random items from list
        List<ItemStack> NewItems = new ArrayList<ItemStack>();
        List<String> usedItems = new ArrayList<String>();
        boolean isUnique = lootTableCache.forcedGet().getOrDefault("UniqueItems", false);
        for (int i = 0; i < items; i++) {
            InteractiveSection itemC = Items.get(Utils.random(0, Items.size() - 1));
            if (isUnique) {
                if (usedItems.contains(itemC.getPath())) {
                    if (usedItems.size() == Items.size())
                        break;
                    i--;
                    continue;
                }
                usedItems.add(itemC.getPath());
            }
            ItemStack a = RealStack.genLoot(itemC);

            if (a != null) {
                if (itemC.has("EnchantList") && Utils.Chance(itemC.getOrDefault("EnchantChance", 101), 100)) {

                    int maxEnchants = itemC.getOrDefault("MaxEnchants", 0);
                    int minEnchants = itemC.getOrDefault("MinEnchants", 0);
                    int enchants = Utils.random(minEnchants, maxEnchants);

                    List<String> EnchantList = itemC.getOrDefault("EnchantList", new ArrayList<String>());
                    List<String> UsedEnchants = new ArrayList<String>();
                    for (int j = 0; j < enchants; j++) {
                        String enchant = Utils.randomFromList(EnchantList);
                        if (enchant == null)
                            continue;
                        if (UsedEnchants.contains(enchant)) {
                            if (UsedEnchants.size() == EnchantList.size())
                                break;
                            j--;
                            UsedEnchants.add(enchant);
                            continue;
                        }
                        // enchant line have 2 parts, ENCHANT_NAME and LEVEL is a format of
                        // random(#anynumber#,#maxlevel#) or a number
                        String[] enchantParts = enchant.split(" ");

                        Enchantment e = Enchantment.getByName(enchantParts[0]);
                        String levelPart = enchantParts[1];
                        levelPart = levelPart.replace("#maxlevel#", String.valueOf(e.getMaxLevel()));
                        String[] levelparts = levelPart.split("to");
                        double levelD = Utils.random(Integer.parseInt(levelparts[0]), Integer.parseInt(levelparts[1]));

                        // remove enchant from list
                        EnchantList = Utils.removeString(EnchantList, enchant);
                        a.addUnsafeEnchantment(e, (int) levelD);
                    }

                }

                // read if item config has SpreadInInventory
                if (itemC.getOrDefault("SpreadInInventory", false)) {
                    // get value of spreadSize , that respond to the number of slots that the item
                    // will be spread
                    int spreadSize = itemC.getOrDefault("SpreadSize", 1);
                    // get rest of empty slots

                    if (spreadSize > a.getAmount())
                        spreadSize = a.getAmount();
                    int rest = items - i;
                    // if the number of slots that the item will be spread is greater than the rest
                    // of empty slots
                    if (spreadSize > rest) {
                        spreadSize = rest;
                    }
                    // create ItemStack copy and use a spread of 30 to 50% of original item amount
                    List<ItemStack> spreadItems = new ArrayList<ItemStack>();
                    int itemAmount = a.getAmount();
                    for (int j = 0; j < spreadSize; j++) {
                        ItemStack spreadItem = a.clone();
                        int am = Utils.random(itemAmount / 2, itemAmount * 2);
                        if (am > itemAmount)
                            am = itemAmount;
                        itemAmount -= am;
                        spreadItem.setAmount(am);
                        spreadItems.add(spreadItem);
                    }
                    // add spread items to NewItems
                    NewItems.addAll(spreadItems);
                } else {
                    NewItems.add(a);
                }

            }
        }

        // get random empty slots
        Integer[] slots = Utils.randomizeArrayOrder(emptySlots);
        // fill empty slots with items
        for (int i = 0; i < NewItems.size(); i++) {
            loot[slots[i]] = NewItems.get(i);
        }

        return loot;
    }

    private static HashMap<InventoryType, Integer[]> emptySlot = new HashMap<InventoryType, Integer[]>();

    static {
        reload();
        emptySlot.put(InventoryType.CHEST,
                new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
                        24, 25, 26 });
        emptySlot.put(InventoryType.DISPENSER, new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
        emptySlot.put(InventoryType.DROPPER, new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
        emptySlot.put(InventoryType.FURNACE, new Integer[] { 0, 1, 2 });
        emptySlot.put(InventoryType.HOPPER, new Integer[] { 0, 1, 2, 3, 4 });
        emptySlot.put(InventoryType.ENCHANTING, new Integer[] { 0, 1, 2 });
        emptySlot.put(InventoryType.ANVIL, new Integer[] { 0, 1, 2 });
        emptySlot.put(InventoryType.BREWING, new Integer[] { 0, 1, 2 });
        emptySlot.put(InventoryType.BEACON, new Integer[] { 0 });
        emptySlot.put(InventoryType.CREATIVE, new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26 });
        emptySlot.put(InventoryType.ENDER_CHEST,
                new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
                        24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 });
        emptySlot.put(InventoryType.BARREL,
                new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
                        24, 25, 26 });
        emptySlot.put(InventoryType.MERCHANT, new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
        emptySlot.put(InventoryType.PLAYER, new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                17, 18, 19, 20, 21, 22, 23, 24, 25, 26 });
        emptySlot.put(InventoryType.WORKBENCH,
                new Integer[] { 0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
                        25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 });
    }
}
