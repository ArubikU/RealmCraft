package dev.arubik.realmcraft.Api;

import java.util.Arrays;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import dev.arubik.realmcraft.Api.Events.LoreEvent;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import lombok.Getter;
import lombok.Setter;

public class LoreParser {
    private Player player;

    public LoreParser(Player player) {
        this.player = player;
    }

    public enum ContextEvent {
        SET_SLOT,
        WINDOW_ITEMS,
        OPEN_WINDOW_MERCHANT,
        ANY
    }

    @Getter
    @Setter
    private ContextEvent contextEvent = ContextEvent.ANY;

    public Function<ItemStack, ItemStack> f = new Function<ItemStack, ItemStack>() {
        @Override
        public ItemStack apply(ItemStack item) {
            if (item == null)
                return null;
            RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Barrier of nullable item reached");
            RealNBT nbt = new RealNBT(item);
            if (player.getGameMode() == GameMode.CREATIVE) {
                if (nbt.contains("ORIGINAL_ITEM")) {
                    ItemStack[] items = nbt.getItemStackArray("ORIGINAL_ITEM");
                    if (items != null) {
                        RealNBT itema = new RealNBT(items[0]);
                        return itema.getItemStack();
                    }

                }
                return item;
            }
            RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Barrier of creative mode reached");
            if (item.getItemMeta() == null)
                return item;
            RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Barrier of nullable item meta reached");
            if (item.getType().isAir())
                return item;
            RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Barrier of air item reached");
            RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Parsing lore for " + item.getType().toString());
            ItemStack[] items = new ItemStack[] { item.clone() };
            nbt.setString("ORIGINAL_ITEM", Utils.itemStackArrayToBase64(items));
            for (RealLore lore : LoreEvent.getLores()) {
                RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Parsing lore " + lore.getClass().toString());
                if (lore.able(item)
                        && lore.able(item, LoreParser.this.getContextEvent())
                        && lore.able(player, item)) {
                    List<DynamicLoreLine> line = lore.getLoreLines();
                    List<String> loreList = Lists.newArrayList();
                    for (DynamicLoreLine l : line) {
                        if (l != null) {
                            String lineAdded = l.parseLine(player, item);
                            loreList.add(lineAdded);
                            RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Added line " + lineAdded);
                        } else {
                            loreList.add("");
                        }
                    }
                    RealMessage.sendConsoleMessage(DebugType.LOREPARSER,
                            "Lore: " + Arrays.deepToString(nbt.getLore().toArray()));
                    nbt.putLoreLines(loreList, lore.getLorePosition());
                }
            }
            item = nbt.getItemStack();
            for (ItemBuildModifier modifier : LoreEvent.getItemBuildModifiers()) {
                RealMessage.sendConsoleMessage(DebugType.LOREPARSER,
                        "Parsing modifier " + modifier.getClass().toString());
                if (modifier.able(item)
                        && modifier.able(item, LoreParser.this.getContextEvent())
                        && modifier.able(player, item)) {
                    item = modifier.modifyItem(player, item);
                }
            }
            return item;
        }
    };

    public ItemStack forceApply(ItemStack item) {
        if (item == null)
            return null;
        RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Barrier of nullable item reached");
        RealNBT nbt = new RealNBT(item);
        RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Barrier of creative mode reached");
        if (item.getItemMeta() == null)
            return item;
        RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Barrier of nullable item meta reached");
        if (item.getType().isAir())
            return item;
        RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Barrier of air item reached");
        RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Parsing lore for " + item.getType().toString());
        for (RealLore lore : LoreEvent.getLores()) {
            RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Parsing lore " + lore.getClass().toString());
            if (lore.able(item)) {
                List<DynamicLoreLine> line = lore.getLoreLines();
                List<String> loreList = Lists.newArrayList();
                for (DynamicLoreLine l : line) {
                    if (l != null) {
                        String lineAdded = l.parseLine(player, item);
                        loreList.add(lineAdded);
                        RealMessage.sendConsoleMessage(DebugType.LOREPARSER, "Added line " + lineAdded);
                    } else {
                        loreList.add("");
                    }
                }
                RealMessage.sendConsoleMessage(DebugType.LOREPARSER,
                        "Lore: " + Arrays.deepToString(nbt.getLore().toArray()));
                nbt.putLoreLines(loreList, lore.getLorePosition());
            }
        }
        item = nbt.getItemStack();
        for (ItemBuildModifier modifier : LoreEvent.getItemBuildModifiers()) {
            RealMessage.sendConsoleMessage(DebugType.LOREPARSER,
                    "Parsing modifier " + modifier.getClass().toString());
            if (modifier.able(item)) {
                item = modifier.modifyItem(player, item);
            }
        }
        return item;
    }

    public Function<List<ItemStack>, List<ItemStack>> f1 = new Function<List<ItemStack>, List<ItemStack>>() {
        @Override
        public List<ItemStack> apply(List<ItemStack> items) {
            List<ItemStack> newItems = Lists.newArrayList();
            for (ItemStack item : items) {
                newItems.add(f.apply(item));
            }
            return newItems;
        }
    };
}