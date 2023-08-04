package dev.arubik.realmcraft.Api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.arubik.realmcraft.Api.Events.BuildEvent;
import dev.arubik.realmcraft.Api.RealNBT.AllowedTypes;
import dev.arubik.realmcraft.Api.RealNBT.NBTTag;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveFolder;
import dev.arubik.realmcraft.FileManagement.InteractiveSection;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.IReplacer.OutputTypes;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import lombok.Getter;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.MMOItems;

public class RealStack {
    private String id;
    private String namespace;
    @Getter
    private int modelid;
    @Getter
    private Material material;
    private RealData data;

    public RealStack(String id, String namespace, int modelid, Material material, RealData data) {
        this.id = id;
        this.namespace = namespace;
        this.modelid = modelid;
        this.material = material;
        this.data = data;
    }

    public RealStack(Material material, int modelid) {
        this.material = material;
        this.modelid = modelid;
        this.namespace = "realmcraft";
        this.id = material.name().toLowerCase();
        this.data = new RealData();
    }

    public ItemStack buildItemStack() {
        ItemStack item = new ItemStack(material);
        if (modelid != 0) {
            item.editMeta(meta -> {
                meta.setCustomModelData(modelid);
            });
        }
        if (data.isLocalizedName) {
            item.editMeta(meta -> {
                meta.setLocalizedName(data.name);
            });
        }
        for (RealEnchantment enchantment : data.enchantments) {
            item.addUnsafeEnchantment(Enchantment.getByName(enchantment.name), enchantment.level);
        }
        item.editMeta(meta -> {

            for (RealAttribute attribute : data.attributes) {
                meta.addAttributeModifier(Attribute.valueOf(attribute.name),
                        new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(), attribute.name + "_realmcraft",
                                attribute.value,
                                attribute.operation, EquipmentSlot.valueOf(attribute.slot)));
            }
            for (RealFlag flag : data.flags) {
                if (flag.value) {
                    meta.addItemFlags(ItemFlag.valueOf(flag.name));
                }
            }
        });
        RealMessage.sendConsoleMessage("Building item: " + item);
        RealNBT nbt = new RealNBT(item);
        for (NBTTag tag : data.nbt) {
            nbt.put(tag);
        }
        if (!data.ignoreNamespaceAndId) {
            nbt.setString("REALMCRAFT_ID", id);
            nbt.setString("REALMCRAFT_NAMESPACE", namespace);
        }
        // if (!data.isLocalizedName) {
        nbt.setDisplayName(data.name);
        // }
        if (data.lore != null) {
            nbt.setLore(data.lore);
        }

        for (RealPotionEffect effect : data.effects) {
            nbt.addPotionEffect(effect);
        }
        if (data.color != null) {
            nbt.setColor(data.color[0], data.color[1], data.color[2]);
        }

        BuildEvent event = new BuildEvent(nbt, id, namespace);
        event.CallEvent();

        return event.getItem().getItemStack();
    }

    public static RealStack fromInteractiveSection(InteractiveSection section) {
        return fromInteractiveSection(section, "");
    }

    public static RealStack fromInteractiveSection(InteractiveSection section, String sec) {
        if (!sec.equals(""))
            sec += ".";

        Material material = Material.getMaterial(section.getOrDefault(sec + "Material", "STONE"));
        if (material == null) {
            material = Material.STONE;
        }
        String id = section.getOrDefault(sec + "id", "realmcraft:stone");
        String name = section.getOrDefault(sec + "Display", "{localizedName}:Material.STONE");
        Boolean isLocalizedName = name.startsWith("{localizedName}:");
        if (isLocalizedName) {
            name = name.substring(16);
        }
        if (name.startsWith("Material.")) {
            name = name.substring(9);
            name = Material.valueOf(name).translationKey();
        }
        Boolean ignoreNamespaceAndId = section.getOrDefault(sec + "IgnoreNamespaceAndId", false);
        // data.ignoreNamespaceAndId = ignoreNamespaceAndId;
        Integer modelid = section.getOrDefault(sec + "ModelID", 0);
        RealData data = new RealData();
        data.ignoreNamespaceAndId = ignoreNamespaceAndId;
        data.isLocalizedName = isLocalizedName;
        data.name = name;
        data.lore = section.getStringList(sec + "Lore", new ArrayList<String>());
        data.enchantments = RealData
                .EnchantmentFromList(section.getStringList(sec + "Enchantments", new ArrayList<String>()));
        data.attributes = RealData
                .AttributeFromList(section.getStringList(sec + "Attributes", new ArrayList<String>()));
        data.flags = RealData.FlagFromList(section.getStringList(sec + "Flags", new ArrayList<String>()));
        data.nbt = RealData.NBTFromList(section.getStringList(sec + "NBT", new ArrayList<String>()));
        data.effects = RealData.PotionEffectFromList(section.getStringList(sec + "Effects", new ArrayList<String>()));

        if (section.has("color")) {
            Integer[] a = (Integer[]) Set.of(section.getOrDefault("red", 0), section.getOrDefault("green", 0),
                    section.getOrDefault("blue", 0)).toArray();
            data.color = a;
        }
        return new RealStack(id, "realmcraft", modelid, material, data);
    }

    public static ItemStack genLoot(InteractiveSection section) {

        String itemType = section.getOrDefault("Type", "VANILLA").toUpperCase();
        ItemStack item = RealNBT.Empty;
        switch (OutputTypes.fromString(itemType)) {
            case REALSTACK: {
                RealStack stack = RealStack.fromInteractiveSection(section);
                item = stack.buildItemStack();
                break;
            }
            case VANILLA:
                item = new ItemStack(Material.getMaterial(section.get("Item", String.class)));
                break;
            case MMOITEMS: {
                String line = section.get("Item", String.class);
                if (line != null && line.contains(":")) {
                    String[] split = line.split(":");
                    List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                            .map(Type::getId).collect(Collectors.toList());
                    // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                    if (types.contains(split[0])) {
                        Type type = MMOItems.plugin.getTypes().get(split[0]);
                        ItemStack stack;
                        stack = MMOItems.plugin.getItem(type, split[1]);
                        if (stack == null) {
                            RealMessage.alert(
                                    "Invalid item id: " + line
                                            + " secure the item is a mmoitem \n example: sword:stone_sword");
                            return RealNBT.Empty;
                        }
                        item = stack;
                    }
                } else {
                    RealMessage.alert(
                            "Invalid item id: " + line + " secure the item is a mmoitem \n example: sword:stone_sword");
                }
                break;
            }
            case MYTHICMOBS: {
                String line = section.get("Item", String.class);
                if (line != null && MythicBukkit.inst().getItemManager().getItem(line).isPresent()) {
                    item = BukkitAdapter
                            .adapt(MythicBukkit.inst().getItemManager().getItem(line).get().generateItemStack(1));
                } else {
                    RealMessage.alert(
                            "Invalid item id: " + line + " secure the item is a mythicitem \n example: wither_crown");
                }
                break;
            }
            default:
                break;

        }
        if (section.has("Range")) {
            String range = section.getOrDefault("Range", "1to10");
            Integer amount = 1;
            String[] split = range.split("to");
            if (split.length == 2) {
                amount = Utils.random(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            } else {
                amount = Integer.parseInt(range);
            }
            item.setAmount((Integer) amount);
        }

        return item;

    }

    public static ItemStack genLoot(InteractiveSection section, Player p) {

        String itemType = section.getOrDefault("Type", "VANILLA").toUpperCase();
        ItemStack item = RealNBT.Empty;
        switch (OutputTypes.fromString(itemType)) {
            case REALSTACK: {
                RealStack stack = RealStack.fromInteractiveSection(section);
                item = stack.buildItemStack();
                break;
            }
            case VANILLA: {
                item = new ItemStack(Material.getMaterial(section.get("Item", String.class)) == null ? Material.AIR
                        : Material.getMaterial(section.get("Item", String.class)));
                break;
            }
            case MMOITEMS: {
                String line = section.get("Item", String.class);
                if (line != null && line.contains(":")) {
                    String[] split = line.split(":");
                    List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                            .map(Type::getId).collect(Collectors.toList());
                    // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                    if (types.contains(split[0])) {
                        Type type = MMOItems.plugin.getTypes().get(split[0]);
                        ItemStack stack;
                        stack = MMOItems.plugin.getItem(type, split[1]);
                        if (stack == null) {
                            RealMessage.alert(
                                    "Invalid item id: " + line
                                            + "secure the item is a mmoitem \n example: sword:stone_sword");
                            return RealNBT.Empty;
                        }
                        item = stack;
                    }
                } else {
                    RealMessage.alert(
                            "Invalid item id: " + line + "secure the item is a mmoitem \n example: sword:stone_sword");
                }
                break;
            }
            case MYTHICMOBS: {
                String line = section.get("Item", String.class);
                if (line != null && MythicBukkit.inst().getItemManager().getItem(line).isPresent()) {
                    item = BukkitAdapter
                            .adapt(MythicBukkit.inst().getItemManager().getItem(line).get().generateItemStack(1));
                } else {
                    if (line != null && line.contains(":")) {
                        String[] split = line.split(":");
                        List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                                .map(Type::getId).collect(Collectors.toList());
                        // RealMessage.sendConsoleMessage("Types: " + Arrays.toString(types.toArray()));
                        if (types.contains(split[0])) {
                            Type type = MMOItems.plugin.getTypes().get(split[0]);
                            ItemStack stack;
                            stack = MMOItems.plugin.getItem(type, split[1]);
                            if (stack == null) {
                                RealMessage.alert(
                                        "Invalid item id: " + line
                                                + "secure the item is a mmoitem \n example: sword:stone_sword");
                                return RealNBT.Empty;
                            }
                            item = stack;
                        }

                        RealMessage.alert(
                                "Item provided is not a mythic item, please check your config \n example: wither_crown\nThe item was processed as MMOITEMS");
                    } else {

                        RealMessage.alert(
                                "Invalid item id: " + line
                                        + " secure the item is a mythicitem \n example: wither_crown");
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
        if (section.has("Range")) {
            String range = section.getOrDefault("Range", "1to10");
            Integer amount = 1;
            String[] split = range.split("to");
            if (split.length == 2) {
                amount = Utils.random(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            } else {
                amount = Integer.parseInt(range);
            }
            item.setAmount((Integer) amount);
        }

        return item;
    }

}