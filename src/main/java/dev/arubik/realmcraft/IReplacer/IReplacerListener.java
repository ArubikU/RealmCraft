package dev.arubik.realmcraft.IReplacer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealCache.RealCacheMap;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import dev.arubik.realmcraft.LootGen.VanillaLootListener;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.lib.gui.PluginInventory;
import io.papermc.paper.event.player.PlayerTradeEvent;
import lombok.Getter;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory;

public class IReplacerListener implements org.bukkit.event.Listener, Depend {

    @Override
    public String[] getDependatsPlugins() {
        return new String[] {};
    }

    public static void register() {
        org.bukkit.Bukkit.getPluginManager().registerEvents(new IReplacerListener(), realmcraft.getInstance());
        // RealMessage.sendConsoleMessage("<yellow>Registered IReplacerListener");
    }

    public static boolean containsNBT(ItemStack stack, List<String> keys) {
        RealNBT nbt = new RealNBT(stack);
        for (String key : keys) {
            if (nbt.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static RealCacheMap<String, InternalReplacerStructure> cache = new RealCacheMap<String, InternalReplacerStructure>();

    @Getter
    private static RealCacheMap<String, ItemStack> cacheMMO = new RealCacheMap<String, ItemStack>();

    public static void clearCache() {
        cache.clear();
        cacheMMO.clear();
    }

    static {
        cache.setRemoveInterval(1200);
        cacheMMO.setRemoveInterval(1200);

        cacheMMO.setFunction(new Function<String, ItemStack>() {
            @Override
            public ItemStack apply(String t) {
                String type = t.split(":")[0];
                String id = t.split(":")[1];
                return cacheMMO.get(type + ":" + id);
            }
        });
    }

    @Nullable
    public static InternalReplacerStructure match(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (stack.getType() == Material.AIR) {
            return null;
        }
        RealNBT nbt = new RealNBT(stack);
        String dump = nbt.dump();
        if (cache.containsKey(dump)) {
            return cache.getCache(dump).isCached() == true ? cache.getCache(dump).forcedGet() : null;
        }

        // add a global ignore list

        for (InternalReplacerStructure structure : IReplacer.getReplacers().values()) {
            if (nbt.containsAny(structure.IgnoreNBT)) {
                continue;
            }
            switch (structure.getType()) {
                case NAME:
                    if (structure.getTypeConfig().has("Name") && structure.getTypeConfig().has("Material")) {
                        String name = IReplacer.getName(nbt);
                        // RealMessage.sendConsoleMessage(name);
                        // RealMessage.sendConsoleMessage(structure.getTypeConfig().get("Name").getAsString());
                        if (name.equals(structure.getTypeConfig().get("Name").getAsString())
                                && stack.getType().toString()
                                        .equalsIgnoreCase(structure.getTypeConfig().get("Material").getAsString())) {

                            cache.put(dump, structure);
                            return structure;

                        }

                    }
                    break;
                case VANILLA:
                    if (structure.getTypeConfig().has("Material")) {
                        if (stack.getType().toString()
                                .equals(structure.getTypeConfig().get("Material").getAsString())) {

                            cache.put(dump, structure);
                            return structure;
                        }

                    }
                    break;
                case NBTTAGMATCH:
                    if (!stack.hasItemMeta()) {
                        break;
                    }
                    if (structure.getTypeConfig().has("NBT-Tag") && structure.getTypeConfig().has("Material")
                            && structure.getTypeConfig().has("NBT-Value")) {
                        if (stack.getType().toString()
                                .equals(structure.getTypeConfig().get("Material").getAsString())) {
                            if (nbt.contains(structure.getTypeConfig().get("NBT-Tag").getAsString())) {
                                if (nbt.getString(structure.getTypeConfig().get("NBT-Tag").getAsString()).equals(
                                        structure.getTypeConfig().get("NBT-Value").getAsString())) {

                                    cache.put(dump, structure);
                                    return structure;

                                }
                            }
                        }

                    }
                    break;
                case CUSTOMMODELDATA: {

                    if (!stack.hasItemMeta()) {
                        break;
                    }
                    if (!stack.getItemMeta().hasCustomModelData()) {
                        break;
                    }
                    if (structure.getTypeConfig().has("Material")
                            && structure.getTypeConfig().has("Custom-Model-Data")) {
                        if (stack.getType().toString()
                                .equals(structure.getTypeConfig().get("Material").getAsString())) {
                            if (stack.getItemMeta().getCustomModelData() == structure.getTypeConfig()
                                    .get("Custom-Model-Data").getAsInt()) {

                                cache.put(dump, structure);
                                return structure;

                            }
                        }
                    }
                    break;
                }
                case LORECONTAINS: {
                    if (!stack.hasItemMeta()) {
                        break;
                    }
                    if (!stack.getItemMeta().hasLore()) {
                        break;
                    }
                    if (structure.getTypeConfig().has("Material")
                            && structure.getTypeConfig().has("Lore")) {
                        if (stack.getType().toString()
                                .equals(structure.getTypeConfig().get("Material").getAsString())) {
                            for (String lore : structure.getTypeConfig().get("Lore").getAsString().split(";")) {
                                for (String line : stack.getItemMeta().getLore()) {
                                    if (lore.contains(line)) {

                                        cache.put(dump, structure);
                                        return structure;

                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                case SKULLOWNER: {
                    if (!stack.hasItemMeta()) {
                        break;
                    }
                    if (!stack.getItemMeta().hasLore()) {
                        break;
                    }
                    if (structure.getTypeConfig().has("Material")
                            && structure.getTypeConfig().has("Skull-Owner")) {
                        if (stack.getType().toString()
                                .equals(structure.getTypeConfig().get("Material").getAsString())) {
                            if (stack.getItemMeta() instanceof SkullMeta meta) {
                                if (meta.getPlayerProfile() != null) {
                                    if (meta.getPlayerProfile().getName() != null) {
                                        if (meta.getPlayerProfile().getName()
                                                .equalsIgnoreCase(
                                                        structure.getTypeConfig().get("Skull-Owner").getAsString())) {

                                            cache.put(dump, structure);
                                            return structure;

                                        }
                                    }
                                }
                            }

                        }
                    }
                    break;
                }
                case NBTCONTAINS: {
                    if (!stack.hasItemMeta()) {
                        break;
                    }
                    if (structure.getTypeConfig().has("Material")
                            && structure.getTypeConfig().has("NBT-Tag")) {
                        if (stack.getType().toString()
                                .equals(structure.getTypeConfig().get("Material").getAsString())) {
                            if (nbt.contains(structure.getTypeConfig().get("NBT-Tag").getAsString())) {
                                cache.put(dump, structure);
                                return structure;

                            }
                        }
                    }
                    break;
                }

            }
        }

        cache.put(dump, null);

        return null;
    }

    public static Optional<InternalReplacerStructure> matchOpt(ItemStack stack) {
        return Optional.ofNullable(match(stack));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void itemPickup(EntityPickupItemEvent e) {
        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.Pickup-Event-Enabled", true)) {
            return;
        }
        if (e.getItem().getItemStack() != null) {
            InternalReplacerStructure structure = match(e.getItem().getItemStack());
            if (structure != null) {
                structure.apply(e.getItem().getItemStack(), (item) -> {
                    e.getItem().setItemStack(item);
                }, ReplacementContext.ofEvent(e));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemClickEvent(InventoryClickEvent e) {

        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.Click-Event-Enabled", true)) {
            return;
        }
        if (e.getView() != null) {
            if (e.getView().getTopInventory() != null) {
                // get name of container
                InventoryView a = e.getView();
                String tittle = a.getTitle();
                List<String> tittleBlacklist = realmcraft.getInteractiveConfig().getStringList(
                        "ireplacer-tittle-blacklist",
                        RealNBT.EmptyList());

                for (String tittleBlacklistLine : tittleBlacklist) {
                    if (tittle.contains(tittleBlacklistLine)) {
                        return;
                    }
                }

            }
        }
        if (e.getClickedInventory() != null) {
            if (e.getCursor() != null) {
                InternalReplacerStructure structure = match(e.getCursor());
                if (structure != null) {
                    structure.apply(e.getCursor(), (item) -> {
                        e.setCursor(item);
                    }, ReplacementContext.ofEvent(e));

                }
            }
        }

    }

    @EventHandler
    public void onInventoryMoveEvent(InventoryMoveItemEvent e) {
        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.InventoryMove-Event-Enabled", true)) {
            return;
        }
        if (!(e.getDestination() instanceof PlayerInventory || e.getSource() instanceof PlayerInventory)) {
            return;
        }
        if (e.getSource().getHolder() instanceof PluginInventory) {
            return;
        }

        if (e.getItem() != null) {
            InternalReplacerStructure structure = match(e.getItem());
            if (structure != null) {
                structure.apply(e.getItem(), (item) -> {
                    e.setItem(item);
                }, ReplacementContext.ofEvent(e));
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.InventoryOpen-Event-Enabled", true)) {
            return;
        }

        if (event.getInventory().getHolder() == null) {
            return;
        }

        if (event.getView() != null) {
            if (event.getView().getTopInventory() != null) {
                // get name of container
                InventoryView a = event.getView();
                String tittle = a.getTitle();
                List<String> tittleBlacklist = realmcraft.getInteractiveConfig().getStringList(
                        "ireplacer-tittle-blacklist",
                        RealNBT.EmptyList());

                for (String tittleBlacklistLine : tittleBlacklist) {
                    if (tittle.contains(tittleBlacklistLine)) {
                        return;
                    }
                }

            }
        }

        InventoryHolder holder = event.getInventory().getHolder();
        Block block;
        if (holder instanceof DoubleChest) {
            if (((DoubleChest) holder).getLeftSide() instanceof BlockInventoryHolder) {
                block = ((BlockInventoryHolder) ((DoubleChest) holder).getLeftSide()).getBlock();
                if (block.getMetadata("ireplacer").size() > 0) {
                    return;
                }

                this.convertInventory(((DoubleChest) holder).getLeftSide().getInventory(),
                        ReplacementContext.ofEvent(event));
                block.setMetadata("ireplacer", new FixedMetadataValue(realmcraft.getInstance(), true));
            }

            holder = ((DoubleChest) holder).getRightSide();
        }

        if (holder instanceof BlockInventoryHolder) {
            block = ((BlockInventoryHolder) holder).getBlock();

            if (block.getMetadata("ireplacer").size() > 0) {
                return;
            }

            this.convertInventory(event.getInventory(), ReplacementContext.ofEvent(event));
            block.setMetadata("ireplacer", new FixedMetadataValue(realmcraft.getInstance(), true));
        } else if (holder instanceof StorageMinecart) {
            StorageMinecart minecart = (StorageMinecart) holder;
            if (minecart.hasMetadata("ireplacer")) {
                return;
            }

            this.convertInventory(event.getInventory(), ReplacementContext.ofEvent(event));
            minecart.setMetadata("ireplacer", new FixedMetadataValue(realmcraft.getInstance(), true));
        }
    }

    @EventHandler
    public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {

        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.ItemCraft-Enabled", true)) {
            return;
        }
        if (event.getInventory().getViewers().get(0) instanceof Player) {
            ItemStack result = event.getInventory().getResult();
            if (result == null || result.getType() == Material.AIR) {
                return;
            }
            String hashed = Utils.itemStackArrayToBase64(new ItemStack[] { result });
            InternalReplacerStructure structure = match(result);
            if (structure == null) {
                return;
            }
            structure.apply(result, (item) -> {

                RealNBT nbt = new RealNBT(item);
                nbt.setString("IReplacer", hashed);
                item = nbt.getItemStack();
                event.getInventory().setResult(item);

            }, ReplacementContext.ofEvent(event));
        }

    }

    @EventHandler
    public void onItemCraftEvent(CraftItemEvent event) {

        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.ItemCraft-Enabled", true)) {
            return;
        }
        if (event.getAction() == InventoryAction.NOTHING || event.getAction() == InventoryAction.CLONE_STACK) {
            event.setCancelled(true);
            return;
        }
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            RealNBT nbt = new RealNBT(event.getCurrentItem());
            if (!nbt.contains("IReplacer"))
                return;
            String hashed = nbt.getString("IReplacer");
            ItemStack[] items = Utils.itemStackArrayFromBase64(hashed);
            ItemStack item = items[0];
            InternalReplacerStructure structure;
            try {
                structure = match(item);
            } catch (Throwable e) {
                event.setCancelled(true);
                return;
            }
            if (structure == null) {
                return;
            }
            if (event.isShiftClick()) {
                Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
                    convertInventory(player.getInventory(), ReplacementContext.ofEvent(event));
                }, 0);
                return;
            }

            structure.apply(item, (output) -> {
                event.setCurrentItem(output);
            }, ReplacementContext.ofEvent(event));
        }

    }

    @EventHandler
    public void onPlayerTradeEvent(PlayerTradeEvent event) {
        convertInventory(event.getPlayer().getInventory(), ReplacementContext.ofEvent(event));
    }

    public void convertInventory(Inventory inv, ReplacementContext context) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            RealNBT nbt = new RealNBT(item);
            if (!nbt.contains("IReplacer"))
                continue;
            String hashed = nbt.getString("IReplacer");
            ItemStack[] items = Utils.itemStackArrayFromBase64(hashed);
            ItemStack item2 = items[0];
            InternalReplacerStructure structure = match(item2);
            if (structure == null) {
                continue;
            }
            int slot = i;

            structure.apply(item2, (output) -> {
                inv.setItem(slot, output);
            }, context);
            inv.setItem(i, item2);
        }
    }

    @EventHandler
    public void onFurnaceSmeltEvent(FurnaceSmeltEvent event) {

        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.FurnaceSmelt-Enabled", true)) {
            return;
        }
        ItemStack item = event.getResult();
        InternalReplacerStructure structure = match(item);
        if (structure == null) {
            return;
        }

        structure.apply(event.getResult(), (output) -> {
            event.setResult(output);
        }, ReplacementContext.ofEvent(event));

    }

    private static Random seed = new Random();

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onLootGen(LootGenerateEvent event) {

        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.LootGen-Enabled", true)) {
            return;
        }
        if (event.getInventoryHolder() instanceof Container container) {
            long longseed = seed.nextLong(0, 100000000);
            seed.setSeed(longseed);
            ReplacementContext context = ReplacementContext.ofEvent(event);
            Collection<ItemStack> items = event.getLootTable().populateLoot(seed, event.getLootContext());
            List<ItemStack> newItems = items.stream().map((ItemStack item) -> {
                if (item == null || item.getType() == Material.AIR) {
                    return item;
                }
                InternalReplacerStructure structure = match(item);
                if (structure == null) {
                    return item;
                }
                ItemStack[] items2 = new ItemStack[] { item };

                structure.apply(item, (output) -> {
                    items2[0] = output;
                }, context);
                return items2[0];

            }).toList();
            newItems = VanillaLootListener.verifyLoot(newItems, event);
            event.setLoot(newItems);
            container.getInventory().setContents(newItems.toArray(ItemStack[]::new));
            if (RealMessage.isDebugEnabled(DebugType.INFO)) {
                RealMessage.sendConsoleMessage(DebugType.INFO,
                        "Generated loot on <0> <1> <2> <3> seed:<4> loottable:<5>",
                        context.getLocation().getBlockX() + "", context.getLocation().getBlockY() + "",
                        context.getLocation().getBlockZ() + "", context.getLocation().getWorld().getName(),
                        longseed + "", event.getLootTable().getKey().toString());
            }

        } else if (event.getInventoryHolder() instanceof Entity entity) {

            if (entity instanceof StorageMinecart container) {

                Random seed = new Random();
                long longseed = seed.nextLong(0, 100000000);
                ReplacementContext context = ReplacementContext.ofEvent(event);
                Collection<ItemStack> items = event.getLootTable().populateLoot(seed, event.getLootContext());
                List<ItemStack> newItems = items.stream().map((ItemStack item) -> {
                    if (item == null || item.getType() == Material.AIR) {
                        return item;
                    }
                    InternalReplacerStructure structure = match(item);
                    if (structure == null) {
                        return item;
                    }
                    ItemStack[] items2 = new ItemStack[] { item };

                    structure.apply(item, (output) -> {
                        items2[0] = output;
                    }, context);
                    return items2[0];

                }).toList();
                newItems = VanillaLootListener.verifyLoot(newItems, event);
                event.setLoot(newItems);
                container.getInventory().setContents(newItems.toArray(ItemStack[]::new));
                if (RealMessage.isDebugEnabled(DebugType.INFO)) {
                    RealMessage.sendConsoleMessage(DebugType.INFO, "Generated loot on <0> <1> <2> <3> seed:<4>",
                            context.getLocation().getBlockX() + "", context.getLocation().getBlockY() + "",
                            context.getLocation().getBlockZ() + "", context.getLocation().getWorld().getName(),
                            longseed + "");
                }
            }
        }
    }

}
