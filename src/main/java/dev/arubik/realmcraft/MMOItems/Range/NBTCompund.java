package dev.arubik.realmcraft.MMOItems.Range;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.bukkit.utils.lib.lang3.Validate;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ColorData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.StringListStat;

public class NBTCompund extends StringListStat {

    public NBTCompund(String id, Material mat, String name, String[] lore, String[] types,
            Material[] materials) {
        super(id, mat, name, lore, types, materials);
    }

    public NBTCompund() {
        super("Compound", Material.BOOK, "Set nbt Compound", new String[] {},
                new String[] { "all" });
    }

    public static void register() {
        RealMessage.Found("MMOItems found starting register of Compound");
        MMOItems.plugin.getStats().register("COMPOUND", new NBTCompund());
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {
        try {
            Class<?> itemClass = item.getClass();
            Field itemStackfield = itemClass.getField("item");
            itemStackfield.setAccessible(true);
            ItemStack itemStack = (ItemStack) itemStackfield.get(item);
            RealNBT nbt = new RealNBT(itemStack);
            for (String str : data.getList()) {
                String[] split = str.split(" ");
                String tag = split[0];
                String value = split[1];

                // tag split by "." to get the path
                String[] tagSplit = tag.split(",");
                // get size of tagSplit
                RealMessage.sendRaw("TagSplit size: " + tagSplit.length);
                if (tagSplit.length == 1) {
                    nbt.setString(tag, value);
                } else if (tagSplit.length == 2) {
                    JsonBuilder json = new JsonBuilder();
                    json.append(tagSplit[1], value);
                    nbt.setJsonElement(tagSplit[0], json.toJson());
                }
            }
            itemStackfield.set(item, nbt.getItemStack());
            itemStackfield.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}