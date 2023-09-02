package dev.arubik.realmcraft.Api;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.Events.LoreEvent;
import dev.arubik.realmcraft.Api.RealCache.RealCacheMap;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import dev.arubik.realmcraft.IReplacer.IReplacerListener;
import dev.arubik.realmcraft.IReplacer.OutputTypes;
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

    public static void clearCache() {
    }

    public Function<ItemStack, ItemStack> f = new Function<ItemStack, ItemStack>() {
        @Override
        public ItemStack apply(ItemStack item) {
            if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE)
                return item;
            if (item == null)
                return null;
            if (!item.hasItemMeta()) {
                return item;
            }
            if (item.getType().isAir())
                return item;
            if (!item.getItemMeta().hasLore()) {
                return item;
            }
            if (!item.getItemMeta().hasDisplayName()) {
                return item;
            }
            RealNBT nbt = new RealNBT(item);
            if (realmcraft.LoreProtocolParser) {
                for (RealLore lore : LoreEvent.getLores()) {
                    if (lore.able(nbt)
                            && lore.able(nbt, LoreParser.this.getContextEvent())
                            && lore.able(player, nbt)) {
                        List<DynamicLoreLine> line = lore.getLoreLines();
                        List<String> loreList = Lists.newArrayList();
                        for (DynamicLoreLine l : line) {
                            if (l != null) {
                                String lineAdded = l.parseLine(player, nbt);
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
            }
            if (realmcraft.ModifierProtocolParser) {
                for (ItemBuildModifier modifier : LoreEvent.getItemBuildModifiers()) {
                    RealMessage.sendConsoleMessage(DebugType.LOREPARSER,
                            "Parsing modifier " + modifier.getClass().toString());
                    if (modifier.able(nbt)
                            && modifier.able(nbt, LoreParser.this.getContextEvent())
                            && modifier.able(player, nbt)) {
                        nbt = modifier.modifyItem(player, nbt);
                    }
                }
            }
            item = nbt.getItemStack();
            return item; // RealNBT.fromItemStack(item).removedInvisibleNBT().getItemStack();
        }
    };

    public ItemStack forceApply(ItemStack item) {
        if (item == null)
            return null;
        if (item.getType().isAir())
            return item;
        RealNBT nbt = new RealNBT(item);
        for (RealLore lore : LoreEvent.getLores()) {
            if (lore.able(nbt)) {
                List<DynamicLoreLine> line = lore.getLoreLines();
                List<String> loreList = Lists.newArrayList();
                for (DynamicLoreLine l : line) {
                    if (l != null) {
                        String lineAdded = l.parseLine(player, nbt);
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
        for (ItemBuildModifier modifier : LoreEvent.getItemBuildModifiers()) {
            RealMessage.sendConsoleMessage(DebugType.LOREPARSER,
                    "Parsing modifier " + modifier.getClass().toString());
            if (modifier.able(nbt)) {
                nbt = modifier.modifyItem(player, nbt);
            }
        }
        item = nbt.getItemStack();
        IReplacerListener.mmoitemsPreview(item, IReplacerListener.match(item));
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