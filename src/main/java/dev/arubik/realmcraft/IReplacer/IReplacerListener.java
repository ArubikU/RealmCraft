package dev.arubik.realmcraft.IReplacer;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.SkullMeta;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.RealStack;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Api.RealCache.RealCacheMap;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.lib.gui.PluginInventory;
import io.papermc.paper.event.player.PlayerTradeEvent;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
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

    public String parsePlaceholders(String toParse, LivingEntity entity, ItemStack stack) {
        toParse = toParse.replace("%player%", entity.getName());
        toParse = toParse.replace("%item%", stack.getType().toString());
        toParse = toParse.replace("%amount%", stack.getAmount() + "");
        // %uuid% %world% %x% %y% %z% %pitch% %yaw% & if e.getEntity() instanceof Player
        // parse PlaceholderAPI
        toParse = toParse.replace("%uuid%", entity.getUniqueId().toString());
        toParse = toParse.replace("%world%", entity.getWorld().getName());
        toParse = toParse.replace("%x%", entity.getLocation().getBlockX() + "");
        toParse = toParse.replace("%y%", entity.getLocation().getBlockY() + "");
        toParse = toParse.replace("%z%", entity.getLocation().getBlockZ() + "");
        toParse = toParse.replace("%pitch%", entity.getLocation().getPitch() + "");
        toParse = toParse.replace("%yaw%", entity.getLocation().getYaw() + "");
        toParse = toParse.replace("%itemname%", stack.getItemMeta().getDisplayName());
        if (entity instanceof Player) {
            if (realmcraft.getInstance().getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                toParse = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders((Player) entity, toParse);
            }
        }
        return toParse;
    }

    @EventHandler
    public void itemPickup(EntityPickupItemEvent e) {
        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.Pickup-Event-Enabled", true)) {
            return;
        }
        if (e.getItem().getItemStack() != null) {
            InternalReplacerStructure structure = match(e.getItem().getItemStack());
            if (structure != null) {
                switch (structure.getOutputType()) {
                    case COMMAND:
                        if (structure.getOutputConfig().has("Command")) {
                            String command = structure.getOutputConfig().get("Command").getAsString();
                            command = parsePlaceholders(command, e.getEntity(), e.getItem().getItemStack());
                            if (e.getEntity() instanceof Player) {
                                if (realmcraft.getInstance().getServer().getPluginManager()
                                        .getPlugin("PlaceholderAPI") != null) {
                                    command = me.clip.placeholderapi.PlaceholderAPI
                                            .setPlaceholders((Player) e.getEntity(), command);
                                }
                            }
                            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);
                            if (structure.getOutputConfig().has("Remove-Item")
                                    && structure.getOutputConfig().get("Remove-Item").getAsBoolean()) {
                                e.getItem().setItemStack(RealNBT.Empty);
                                e.getItem().remove();
                            }
                        }
                        break;
                    case REALSTACK: {
                        RealStack stack = RealStack
                                .fromInteractiveSection(structure.getSection().getSection("Output-Config"));
                        ItemStack item = stack.buildItemStack();
                        item.setAmount(e.getItem().getItemStack().getAmount());
                        if (structure.getOutputConfig().has("Pass-Enchantments")
                                && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                            item.addUnsafeEnchantments(e.getItem().getItemStack().getEnchantments());
                        }
                        if (structure.getOutputConfig().has("Pass-CMD")
                                && structure.getOutputConfig().get("Pass-CMD")
                                        .getAsBoolean()) {
                            Integer custommoldeldata = e.getItem().getItemStack().getItemMeta().getCustomModelData();
                            item.editMeta(meta -> {
                                meta.setCustomModelData(custommoldeldata);
                            });
                        }
                        e.getItem().setItemStack(item);
                        break;
                    }
                    case MMOITEMS: {
                        if (structure.getOutputConfig().has("Item")) {
                            String line = structure.getOutputConfig().get("Item").getAsString();
                            String[] split = line.split(":");
                            List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                                    .map(Type::getId).collect(Collectors.toList());
                            // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                            if (types.contains(split[0])) {
                                Type type = MMOItems.plugin.getTypes().get(split[0]);
                                ItemStack stack;
                                if (e.getEntity() instanceof Player) {
                                    stack = MMOItems.plugin.getItem(type, split[1],
                                            PlayerData.get((Player) e.getEntity()));
                                } else {
                                    stack = cacheMMO.get(type + ":" + split[1]);
                                }
                                if (stack == null) {
                                    break;
                                }
                                stack.setAmount(e.getItem().getItemStack().getAmount());
                                if (structure.getOutputConfig().has("Pass-Enchantments")
                                        && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                                    stack.addUnsafeEnchantments(e.getItem().getItemStack().getEnchantments());
                                }
                                if (structure.getOutputConfig().has("Pass-CMD")
                                        && structure.getOutputConfig().get("Pass-CMD")
                                                .getAsBoolean()) {
                                    Integer custommoldeldata = e.getItem().getItemStack().getItemMeta()
                                            .getCustomModelData();
                                    stack.editMeta(meta -> {
                                        meta.setCustomModelData(custommoldeldata);
                                    });
                                }
                                e.getItem().setItemStack(stack);
                            }
                        }
                        break;
                    }
                    case MYTHICMOBS: {
                        if (structure.getOutputConfig().has("Item")) {
                            String line = structure.getOutputConfig().get("Item").getAsString();
                            String[] split = line.split(":");
                            if (MythicBukkit.inst().getItemManager().getItem(split[0]).isPresent()) {
                                MythicItem mythicItem = MythicBukkit.inst().getItemManager().getItem(split[0]).get();
                                ItemStack stack = BukkitAdapter
                                        .adapt(mythicItem.generateItemStack(Integer.parseInt(split[1])));
                                if (stack == null) {
                                    break;
                                }
                                stack.setAmount(e.getItem().getItemStack().getAmount());
                                if (structure.getOutputConfig().has("Pass-Enchantments")
                                        && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                                    stack.addUnsafeEnchantments(e.getItem().getItemStack().getEnchantments());
                                }
                                if (structure.getOutputConfig().has("Pass-CMD")
                                        && structure.getOutputConfig().get("Pass-CMD")
                                                .getAsBoolean()) {
                                    Integer custommoldeldata = e.getItem().getItemStack().getItemMeta()
                                            .getCustomModelData();
                                    stack.editMeta(meta -> {
                                        meta.setCustomModelData(custommoldeldata);
                                    });
                                }
                                e.getItem().setItemStack(stack);
                            }
                        }
                        break;
                    }

                    case VANILLA:
                        if (structure.getSection().has("Output-Config.Material")) {
                            if (Material
                                    .valueOf(structure.getSection().get("Output-Config.Material").toString()) == null) {
                                break;
                            }
                            ItemStack stack = new ItemStack(
                                    Material.valueOf(structure.getSection().get("Output-Config.Material").toString()));
                            stack.setAmount(e.getItem().getItemStack().getAmount());
                            if (structure.getOutputConfig().has("Pass-Enchantments")
                                    && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                                stack.addUnsafeEnchantments(e.getItem().getItemStack().getEnchantments());
                            }
                            if (structure.getOutputConfig().has("Pass-CMD")
                                    && structure.getOutputConfig().get("Pass-CMD")
                                            .getAsBoolean()) {
                                Integer custommoldeldata = e.getItem().getItemStack().getItemMeta()
                                        .getCustomModelData();
                                stack.editMeta(meta -> {
                                    meta.setCustomModelData(custommoldeldata);
                                });
                            }
                            e.getItem().setItemStack(stack);
                        }
                    default:
                        break;

                }
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
                    switch (structure.getOutputType()) {
                        case COMMAND:
                            if (structure.getOutputConfig().has("Command")) {
                                String command = structure.getOutputConfig().get("Command").getAsString();
                                command = parsePlaceholders(command, e.getWhoClicked(), e.getCursor());
                                if (e.getWhoClicked() instanceof Player) {
                                    if (realmcraft.getInstance().getServer().getPluginManager()
                                            .getPlugin("PlaceholderAPI") != null) {
                                        command = me.clip.placeholderapi.PlaceholderAPI
                                                .setPlaceholders((Player) e.getWhoClicked(), command);
                                    }
                                }
                                org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);
                                if (structure.getOutputConfig().has("Remove-Item")
                                        && structure.getOutputConfig().get("Remove-Item").getAsBoolean()) {
                                    e.getCursor().setAmount(0);
                                }
                            }
                            break;
                        case REALSTACK: {
                            RealStack stack = RealStack
                                    .fromInteractiveSection(structure.getSection().getSection("Output-Config"));
                            ItemStack item = stack.buildItemStack();
                            item.setAmount(e.getCursor().getAmount());
                            if (structure.getOutputConfig().has("Pass-Enchantments")
                                    && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                                item.addUnsafeEnchantments(e.getCursor().getEnchantments());
                            }
                            if (structure.getOutputConfig().has("Pass-CMD")
                                    && structure.getOutputConfig().get("Pass-CMD")
                                            .getAsBoolean()) {
                                Integer custommoldeldata = e.getCursor().getItemMeta().getCustomModelData();
                                item.editMeta(meta -> {
                                    meta.setCustomModelData(custommoldeldata);
                                });
                            }
                            e.setCursor(item);
                            break;
                        }
                        case MMOITEMS: {
                            if (structure.getOutputConfig().has("Item")) {
                                String line = structure.getOutputConfig().get("Item").getAsString();
                                String[] split = line.split(":");
                                List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                                        .map(Type::getId).collect(Collectors.toList());
                                // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                                if (types.contains(split[0])) {
                                    Type type = MMOItems.plugin.getTypes().get(split[0]);
                                    ItemStack stack;
                                    if (e.getWhoClicked() instanceof Player) {
                                        stack = MMOItems.plugin.getItem(type, split[1],
                                                PlayerData.get((Player) e.getWhoClicked()));
                                    } else {
                                        stack = cacheMMO.get(type + ":" + split[1]);
                                    }
                                    if (stack == null) {
                                        break;
                                    }
                                    stack.setAmount(e.getCursor().getAmount());
                                    if (structure.getOutputConfig().has("Pass-Enchantments")
                                            && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                                        stack.addUnsafeEnchantments(e.getCursor().getEnchantments());
                                    }
                                    if (structure.getOutputConfig().has("Pass-CMD")
                                            && structure.getOutputConfig().get("Pass-CMD")
                                                    .getAsBoolean()) {
                                        Integer custommoldeldata = e.getCursor().getItemMeta().getCustomModelData();
                                        stack.editMeta(meta -> {
                                            meta.setCustomModelData(custommoldeldata);
                                        });
                                    }
                                    e.setCursor(stack);
                                }
                            }
                        }
                        case MYTHICMOBS: {
                            if (structure.getOutputConfig().has("Item")) {
                                String line = structure.getOutputConfig().get("Item").getAsString();
                                String[] split = line.split(":");
                                if (MythicBukkit.inst().getItemManager().getItem(split[0]).isPresent()) {
                                    MythicItem mythicItem = MythicBukkit.inst().getItemManager().getItem(split[0])
                                            .get();
                                    ItemStack stack = BukkitAdapter
                                            .adapt(mythicItem.generateItemStack(Integer.parseInt(split[1])));
                                    if (stack == null) {
                                        break;
                                    }
                                    stack.setAmount(e.getCursor().getAmount());
                                    if (structure.getOutputConfig().has("Pass-Enchantments")
                                            && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                                        stack.addUnsafeEnchantments(e.getCursor().getEnchantments());
                                    }
                                    if (structure.getOutputConfig().has("Pass-CMD")
                                            && structure.getOutputConfig().get("Pass-CMD")
                                                    .getAsBoolean()) {
                                        Integer custommoldeldata = e.getCursor().getItemMeta().getCustomModelData();
                                        stack.editMeta(meta -> {
                                            meta.setCustomModelData(custommoldeldata);
                                        });
                                    }
                                    e.setCursor(stack);
                                }
                            }
                            break;
                        }
                    }
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
                switch (structure.getOutputType()) {
                    case COMMAND:
                        if (structure.getOutputConfig().has("Command")) {
                            String command = structure.getOutputConfig().get("Command").getAsString();
                            command = parsePlaceholders(command, (LivingEntity) e.getInitiator().getHolder(),
                                    e.getItem());
                            if (e.getInitiator().getHolder() instanceof Player) {
                                if (realmcraft.getInstance().getServer().getPluginManager()
                                        .getPlugin("PlaceholderAPI") != null) {
                                    command = me.clip.placeholderapi.PlaceholderAPI
                                            .setPlaceholders((Player) e.getInitiator().getHolder(), command);
                                }
                            }
                            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);
                            if (structure.getOutputConfig().has("Remove-Item")
                                    && structure.getOutputConfig().get("Remove-Item").getAsBoolean()) {
                                e.getItem().setAmount(0);
                            }
                        }
                        break;
                    case REALSTACK: {
                        RealStack stack = RealStack
                                .fromInteractiveSection(structure.getSection().getSection("Output-Config"));
                        ItemStack item = stack.buildItemStack();
                        item.setAmount(e.getItem().getAmount());
                        if (structure.getOutputConfig().has("Pass-Enchantments")
                                && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                            item.addUnsafeEnchantments(e.getItem().getEnchantments());
                        }

                        if (structure.getOutputConfig().has("Pass-CMD")
                                && structure.getOutputConfig().get("Pass-CMD")
                                        .getAsBoolean()) {
                            Integer custommoldeldata = e.getItem().getItemMeta().getCustomModelData();
                            item.editMeta(meta -> {
                                meta.setCustomModelData(custommoldeldata);
                            });
                        }
                        e.setItem(item);
                        e.getItem().setData(item.getData());
                        e.getItem().setItemMeta(item.getItemMeta());
                        break;
                    }
                    case MMOITEMS: {
                        if (structure.getOutputConfig().has("Item")) {
                            String line = structure.getOutputConfig().get("Item").getAsString();
                            String[] split = line.split(":");
                            List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                                    .map(Type::getId).collect(Collectors.toList());
                            // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                            if (types.contains(split[0])) {
                                Type type = MMOItems.plugin.getTypes().get(split[0]);
                                ItemStack stack;
                                if (e.getInitiator().getHolder() instanceof Player) {
                                    stack = MMOItems.plugin.getItem(type, split[1],
                                            PlayerData.get((Player) e.getInitiator().getHolder()));
                                } else {
                                    stack = cacheMMO.get(type + ":" + split[1]);
                                }
                                if (stack == null) {
                                    break;
                                }
                                stack.setAmount(e.getItem().getAmount());
                                if (structure.getOutputConfig().has("Pass-Enchantments")
                                        && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                                    stack.addUnsafeEnchantments(e.getItem().getEnchantments());
                                }

                                if (structure.getOutputConfig().has("Pass-CMD")
                                        && structure.getOutputConfig().get("Pass-CMD")
                                                .getAsBoolean()) {
                                    Integer custommoldeldata = e.getItem().getItemMeta().getCustomModelData();
                                    stack.editMeta(meta -> {
                                        meta.setCustomModelData(custommoldeldata);
                                    });
                                }
                                e.setItem(stack);
                                e.getItem().setData(stack.getData());
                                e.getItem().setItemMeta(stack.getItemMeta());
                            }
                        }
                    }
                    case MYTHICMOBS: {
                        if (structure.getOutputConfig().has("Item")) {
                            String line = structure.getOutputConfig().get("Item").getAsString();
                            String[] split = line.split(":");
                            if (MythicBukkit.inst().getItemManager().getItem(split[0]).isPresent()) {
                                MythicItem mythicItem = MythicBukkit.inst().getItemManager().getItem(split[0]).get();
                                ItemStack stack = BukkitAdapter
                                        .adapt(mythicItem.generateItemStack(Integer.parseInt(split[1])));
                                if (stack == null) {
                                    break;
                                }
                                stack.setAmount(e.getItem().getAmount());
                                if (structure.getOutputConfig().has("Pass-Enchantments")
                                        && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                                    stack.addUnsafeEnchantments(e.getItem().getEnchantments());
                                }
                                if (structure.getOutputConfig().has("Pass-CMD")
                                        && structure.getOutputConfig().get("Pass-CMD")
                                                .getAsBoolean()) {
                                    Integer custommoldeldata = e.getItem().getItemMeta().getCustomModelData();
                                    stack.editMeta(meta -> {
                                        meta.setCustomModelData(custommoldeldata);
                                    });
                                }
                                e.setItem(stack);
                                e.getItem().setData(stack.getData());
                                e.getItem().setItemMeta(stack.getItemMeta());
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private Set<Byte[]> openChest = new HashSet<Byte[]>();

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.InventoryOpen-Event-Enabled", true)) {
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
        if (event.getInventory().getViewers().get(0) instanceof Player) {

            Player player = (Player) event.getInventory().getViewers().get(0);
            if (!(event.getInventory().getType() == InventoryType.PLAYER
                    || event.getInventory().getType() == InventoryType.CHEST
                    || event.getInventory().getType() == InventoryType.DISPENSER
                    || event.getInventory().getType() == InventoryType.DROPPER
                    || event.getInventory().getType() == InventoryType.HOPPER
                    || event.getInventory().getType() == InventoryType.ENDER_CHEST
                    || event.getInventory().getType() == InventoryType.SHULKER_BOX
                    || event.getInventory().getType() == InventoryType.BARREL
                    || event.getInventory().getType() == InventoryType.BEACON
                    || event.getInventory().getType() == InventoryType.CREATIVE)) {
                return;
            }

            // get location of container
            Location location = event.getInventory().getLocation();
            if (location != null) {
                // Hash location to a string
                String locationString = location.getWorld().getName() + ":" + location.getBlockX() + ":"
                        + location.getBlockY() + ":" + location.getBlockZ();
                // convert locationString to a byte array
                byte[] locationInt = locationString.getBytes();
                Byte[] locationBytes = new Byte[locationInt.length];
                int i = 0;
                for (byte b : locationInt) {
                    locationBytes[i++] = b;
                }

                // convert byte array to a hash
                if (openChest.contains(locationBytes)) {
                    return;
                } else {
                    openChest.add(locationBytes);
                }
                locationInt = null;
                locationBytes = null;
            }

            for (int i = 0; i < event.getInventory().getSize(); i++) {
                ItemStack item = event.getInventory().getItem(i);
                if (item != null) {
                    InternalReplacerStructure structure = match(item);
                    if (structure != null) {
                        switch (structure.getOutputType()) {
                            case COMMAND:
                                if (structure.getOutputConfig().has("Command")) {
                                    String command = structure.getOutputConfig().get("Command").getAsString();
                                    command = parsePlaceholders(command, player, item);
                                    if (realmcraft.getInstance().getServer().getPluginManager()
                                            .getPlugin("PlaceholderAPI") != null) {
                                        command = me.clip.placeholderapi.PlaceholderAPI
                                                .setPlaceholders(player, command);
                                    }
                                    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);
                                    if (structure.getOutputConfig().has("Remove-Item")
                                            && structure.getOutputConfig().get("Remove-Item").getAsBoolean()) {
                                        item.setAmount(0);
                                    }
                                }
                                break;
                            case REALSTACK: {
                                RealStack stack = RealStack
                                        .fromInteractiveSection(structure.getSection().getSection("Output-Config"));
                                ItemStack itemStack = stack.buildItemStack();
                                itemStack.setAmount(item.getAmount());
                                if (structure.getOutputConfig().has("Pass-Enchantments")
                                        && structure.getOutputConfig().get("Pass-Enchantments").getAsBoolean()) {
                                    itemStack.addUnsafeEnchantments(item.getEnchantments());
                                }
                                if (structure.getOutputConfig().has("Pass-CMD")
                                        && structure.getOutputConfig().get("Pass-CMD")
                                                .getAsBoolean()) {
                                    Integer custommoldeldata = item.getItemMeta().getCustomModelData();
                                    itemStack.editMeta(meta -> {
                                        meta.setCustomModelData(custommoldeldata);
                                    });
                                }
                                item = itemStack;
                                break;
                            }
                            case MMOITEMS: {
                                if (structure.getOutputConfig().has("Item")) {
                                    String line = structure.getOutputConfig().get("Item").getAsString();
                                    String[] split = line.split(":");
                                    List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                                            .map(Type::getId).collect(Collectors.toList());
                                    // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                                    if (types.contains(split[0])) {
                                        Type type = MMOItems.plugin.getTypes().get(split[0]);
                                        ItemStack stack;
                                        if (event.getInventory().getHolder() instanceof Player) {
                                            stack = MMOItems.plugin.getItem(type, split[1],
                                                    PlayerData.get((Player) event.getInventory().getHolder()));
                                        } else {
                                            stack = cacheMMO.get(type + ":" + split[1]);
                                        }

                                        if (stack == null) {
                                            break;
                                        }
                                        stack.setAmount(item.getAmount());
                                        if (structure.getOutputConfig().has("Pass-Enchantments")
                                                && structure.getOutputConfig().get("Pass-Enchantments")
                                                        .getAsBoolean()) {
                                            stack.addUnsafeEnchantments(item.getEnchantments());
                                        }
                                        if (structure.getOutputConfig().has("Pass-CMD")
                                                && structure.getOutputConfig().get("Pass-CMD")
                                                        .getAsBoolean()) {
                                            Integer custommoldeldata = item.getItemMeta().getCustomModelData();
                                            stack.editMeta(meta -> {
                                                meta.setCustomModelData(custommoldeldata);
                                            });
                                        }
                                        item = stack;
                                    }
                                }
                            }
                            case MYTHICMOBS: {
                                if (structure.getOutputConfig().has("Item")) {
                                    String line = structure.getOutputConfig().get("Item").getAsString();
                                    String[] split = line.split(":");
                                    if (MythicBukkit.inst().getItemManager().getItem(split[0]).isPresent()) {
                                        MythicItem mythicItem = MythicBukkit.inst().getItemManager().getItem(split[0])
                                                .get();
                                        ItemStack stack = BukkitAdapter
                                                .adapt(mythicItem.generateItemStack(Integer.parseInt(split[1])));
                                        if (stack == null) {
                                            break;
                                        }
                                        stack.setAmount(item.getAmount());
                                        if (structure.getOutputConfig().has("Pass-Enchantments")
                                                && structure.getOutputConfig().get("Pass-Enchantments")
                                                        .getAsBoolean()) {
                                            stack.addUnsafeEnchantments(item.getEnchantments());
                                        }
                                        if (structure.getOutputConfig().has("Pass-CMD")
                                                && structure.getOutputConfig().get("Pass-CMD")
                                                        .getAsBoolean()) {
                                            Integer custommoldeldata = item.getItemMeta().getCustomModelData();
                                            stack.editMeta(meta -> {
                                                meta.setCustomModelData(custommoldeldata);
                                            });
                                        }
                                        item = stack;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                event.getInventory().setItem(i, item);
            }
        }

    }

    public static ItemStack getItemPreviewMMOitems(String type, String id) {
        if (Type.isValid(type)) {
            MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(Type.get(type), id);
            ConfigMMOItem configMMOItem = new ConfigMMOItem(template, 1);
            return configMMOItem.getPreview();
        }
        return null;
    }

    @EventHandler
    public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {

        if (!realmcraft.getInteractiveConfig().getBoolean("IReplacer.ItemCraft-Enabled", true)) {
            return;
        }
        if (event.getInventory().getViewers().get(0) instanceof Player) {
            ItemStack item = event.getInventory().getResult();
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
            String hashed = Utils.itemStackArrayToBase64(new ItemStack[] { item });
            InternalReplacerStructure structure = match(item);
            if (structure == null) {
                return;
            }
            switch (structure.getOutputType()) {
                case MMOITEMS: {

                    if (structure.getOutputConfig().has("Item")) {
                        String line = structure.getOutputConfig().get("Item").getAsString();
                        String[] split = line.split(":");
                        List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                                .map(Type::getId).collect(Collectors.toList());
                        // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                        if (types.contains(split[0])) {
                            Type type = MMOItems.plugin.getTypes().get(split[0]);
                            ItemStack stack = getItemPreviewMMOitems(split[0], split[1]);

                            if (stack == null) {
                                break;
                            }
                            stack.setAmount(item.getAmount());
                            if (structure.getOutputConfig().has("Pass-Enchantments")
                                    && structure.getOutputConfig().get("Pass-Enchantments")
                                            .getAsBoolean()) {
                                stack.addUnsafeEnchantments(item.getEnchantments());
                            }
                            if (structure.getOutputConfig().has("Pass-CMD")
                                    && structure.getOutputConfig().get("Pass-CMD")
                                            .getAsBoolean()) {
                                Integer custommoldeldata = item.getItemMeta().getCustomModelData();
                                stack.editMeta(meta -> {
                                    meta.setCustomModelData(custommoldeldata);
                                });
                            }
                            item = stack;
                        }
                    }
                    break;
                }
            }
            RealNBT nbt = new RealNBT(item);
            nbt.setString("IReplacer", hashed);
            item = nbt.getItemStack();
            event.getInventory().setResult(item);
        }

    }

    public static ItemStack mmoitemsPreview(ItemStack item, InternalReplacerStructure structure) {
        if (structure == null)
            return item;
        if (structure.getOutputConfig().has("Item")) {
            String line = structure.getOutputConfig().get("Item").getAsString();
            String[] split = line.split(":");
            List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                    .map(Type::getId).collect(Collectors.toList());
            // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
            if (types.contains(split[0])) {
                ItemStack stack = getItemPreviewMMOitems(split[0], split[1]);
                if (stack == null) {
                    return item;
                }
                stack.setAmount(item.getAmount());
                if (structure.getOutputConfig().has("Pass-Enchantments")
                        && structure.getOutputConfig().get("Pass-Enchantments")
                                .getAsBoolean()) {
                    stack.addUnsafeEnchantments(item.getEnchantments());
                }
                if (structure.getOutputConfig().has("Pass-CMD")
                        && structure.getOutputConfig().get("Pass-CMD")
                                .getAsBoolean()) {
                    Integer custommoldeldata = item.getItemMeta().getCustomModelData();
                    stack.editMeta(meta -> {
                        meta.setCustomModelData(custommoldeldata);
                    });
                }
                item = stack;
            }
        }
        return item;
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
            switch (structure.getOutputType()) {
                case MMOITEMS: {
                    if (event.isShiftClick()) {
                        // event.getInventory().setResult(item);
                        // event.setCursor(item);
                        // event.setCurrentItem(item);
                        Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
                            convertPlayerInventory(player);
                        }, 0);
                        return;
                    }

                    if (structure.getOutputConfig().has("Item")) {
                        String line = structure.getOutputConfig().get("Item").getAsString();
                        String[] split = line.split(":");
                        List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                                .map(Type::getId).collect(Collectors.toList());
                        // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                        if (types.contains(split[0])) {
                            Type type = MMOItems.plugin.getTypes().get(split[0]);
                            ItemStack stack;
                            if (event.getInventory().getHolder() instanceof Player) {
                                stack = MMOItems.plugin.getItem(type, split[1],
                                        PlayerData.get((Player) event.getInventory().getHolder()));
                            } else {
                                stack = cacheMMO.get(type + ":" + split[1]);
                            }

                            if (stack == null) {
                                break;
                            }
                            stack.setAmount(item.getAmount());
                            if (structure.getOutputConfig().has("Pass-Enchantments")
                                    && structure.getOutputConfig().get("Pass-Enchantments")
                                            .getAsBoolean()) {
                                stack.addUnsafeEnchantments(item.getEnchantments());
                            }
                            if (structure.getOutputConfig().has("Pass-CMD")
                                    && structure.getOutputConfig().get("Pass-CMD")
                                            .getAsBoolean()) {
                                Integer custommoldeldata = item.getItemMeta().getCustomModelData();
                                stack.editMeta(meta -> {
                                    meta.setCustomModelData(custommoldeldata);
                                });
                            }
                            item = stack;
                        }
                    }
                    break;
                }
            }
            event.setCurrentItem(item);
        }

    }

    @EventHandler
    public void onPlayerTradeEvent(PlayerTradeEvent event) {
        convertPlayerInventoryTWO(event.getPlayer());
    }

    public void convertPlayerInventory(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
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
            switch (structure.getOutputType()) {
                case MMOITEMS: {
                    if (structure.getOutputConfig().has("Item")) {
                        String line = structure.getOutputConfig().get("Item").getAsString();
                        String[] split = line.split(":");
                        List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                                .map(Type::getId).collect(Collectors.toList());
                        // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                        if (types.contains(split[0])) {
                            Type type = MMOItems.plugin.getTypes().get(split[0]);
                            ItemStack stack;
                            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Player) {
                                stack = MMOItems.plugin.getItem(type, split[1],
                                        PlayerData
                                                .get((Player) player.getOpenInventory().getTopInventory().getHolder()));
                            } else {
                                stack = cacheMMO.get(type + ":" + split[1]);
                            }

                            if (stack == null) {
                                break;
                            }
                            stack.setAmount(item2.getAmount());
                            if (structure.getOutputConfig().has("Pass-Enchantments")
                                    && structure.getOutputConfig().get("Pass-Enchantments")
                                            .getAsBoolean()) {
                                stack.addUnsafeEnchantments(item2.getEnchantments());
                            }
                            if (structure.getOutputConfig().has("Pass-CMD")
                                    && structure.getOutputConfig().get("Pass-CMD")
                                            .getAsBoolean()) {
                                Integer custommoldeldata = item2.getItemMeta().getCustomModelData();
                                stack.editMeta(meta -> {
                                    meta.setCustomModelData(custommoldeldata);
                                });
                            }
                            item2 = stack;
                        }
                    }
                    break;
                }
                case MMODIGICONVERT: {

                }
            }
            player.getInventory().setItem(i, item2);
        }
    }

    public void convertPlayerInventoryTWO(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            InternalReplacerStructure structure = match(item);
            if (structure == null) {
                continue;
            }
            switch (structure.getOutputType()) {
                case MMOITEMS: {
                    if (structure.getOutputConfig().has("Item")) {
                        String line = structure.getOutputConfig().get("Item").getAsString();
                        String[] split = line.split(":");
                        List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                                .map(Type::getId).collect(Collectors.toList());
                        // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                        if (types.contains(split[0])) {
                            Type type = MMOItems.plugin.getTypes().get(split[0]);
                            ItemStack stack;
                            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Player) {
                                stack = MMOItems.plugin.getItem(type, split[1],
                                        PlayerData
                                                .get((Player) player.getOpenInventory().getTopInventory().getHolder()));
                            } else {
                                stack = cacheMMO.get(type + ":" + split[1]);
                            }

                            if (stack == null) {
                                break;
                            }
                            stack.setAmount(item.getAmount());
                            if (structure.getOutputConfig().has("Pass-Enchantments")
                                    && structure.getOutputConfig().get("Pass-Enchantments")
                                            .getAsBoolean()) {
                                stack.addUnsafeEnchantments(item.getEnchantments());
                            }
                            if (structure.getOutputConfig().has("Pass-CMD")
                                    && structure.getOutputConfig().get("Pass-CMD")
                                            .getAsBoolean()) {
                                Integer custommoldeldata = item.getItemMeta().getCustomModelData();
                                stack.editMeta(meta -> {
                                    meta.setCustomModelData(custommoldeldata);
                                });
                            }
                            item = stack;
                        }
                    }
                    break;
                }
            }
            player.getInventory().setItem(i, item);
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
        switch (structure.getOutputType()) {
            case MMOITEMS: {

                if (structure.getOutputConfig().has("Item")) {
                    String line = structure.getOutputConfig().get("Item").getAsString();
                    String[] split = line.split(":");
                    List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                            .map(Type::getId).collect(Collectors.toList());
                    // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                    if (types.contains(split[0])) {
                        Type type = MMOItems.plugin.getTypes().get(split[0]);
                        ItemStack stack = cacheMMO.get(type + ":" + split[1]);

                        if (stack == null) {
                            break;
                        }
                        stack.setAmount(item.getAmount());
                        if (structure.getOutputConfig().has("Pass-Enchantments")
                                && structure.getOutputConfig().get("Pass-Enchantments")
                                        .getAsBoolean()) {
                            stack.addUnsafeEnchantments(item.getEnchantments());
                        }
                        if (structure.getOutputConfig().has("Pass-CMD")
                                && structure.getOutputConfig().get("Pass-CMD")
                                        .getAsBoolean()) {
                            Integer custommoldeldata = item.getItemMeta().getCustomModelData();
                            stack.editMeta(meta -> {
                                meta.setCustomModelData(custommoldeldata);
                            });
                        }
                        item = stack;
                    }
                }
                break;
            }
        }
        event.setResult(item);

    }

}
