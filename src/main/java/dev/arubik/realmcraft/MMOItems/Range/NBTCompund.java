package dev.arubik.realmcraft.MMOItems.Range;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Api.RealNBT.AllowedTypes;
import dev.arubik.realmcraft.Api.RealNBT.NBTTag;
import dev.arubik.realmcraft.Api.RealReflect.RealField;
import dev.arubik.realmcraft.Api.RealReflect.RealField.RealClazz;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.bukkit.utils.lib.lang3.Validate;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.event.ItemBuildEvent;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.StaffSpiritStat.StaffSpirit;
import net.Indyuce.mmoitems.stat.data.ColorData;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import net.Indyuce.mmoitems.stat.type.StringStat;

public class NBTCompund extends StringStat implements Listener {

    public NBTCompund(String id, Material mat, String name, String[] lore, String[] types,
            Material[] materials) {
        super(id, mat, name, lore, types, materials);
    }

    public NBTCompund() {
        super("NewCompound", Material.BOOK, "Set nbt Compound", new String[] {},
                new String[] { "all" });
    }

    public static void register() {
        RealMessage.Found("MMOItems found starting register of Compound");
        MMOItems.plugin.getStats().register("NewCompound", new NBTCompund());
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        item.addItemTag(new ItemTag("COMPOUND", data.toString()));
        RealMessage.sendRaw(data.toString());
    }

    @EventHandler
    public void onItemBuildEvent(ItemBuildEvent event) {
        RealNBT nbt = new RealNBT(event.getItemStack());
        if (nbt.contains("compound")) {
            String string = nbt.getString("compound");
            // compound example
            // {key1:{thata="1",integerval=1,booleanval=true};key2:{thata="1",integerval=1,booleanval=true}}

            // complete logic
            List<NBTTag> list = new ArrayList<NBTTag>();
            // get all main keys, like key1 and key2
            String[] keys = string.split(";");
            for (String key : keys) {
                String string1 = key.split(":")[0];
                key = key.replace(string1 + ":", "");
                // get all values of key1
                String[] values = key.split(",");
                // create new nbt tag
                List<NBTTag> tag = new ArrayList<NBTTag>();

                for (String value : values) {
                    // get all values of key1
                    String[] valueSplit = value.split("=");
                    // add value to nbt tag
                    tag.add(new NBTTag(valueSplit[0], null, valueSplit[1]));
                }
                // add nbt tag to list
                list.add(new NBTTag(string1, AllowedTypes.LIST, tag));
            }

            for (NBTTag tag : list) {
                nbt.put(tag);
            }

        }

        event.setItemStack(nbt.getItemStack());
    }

}