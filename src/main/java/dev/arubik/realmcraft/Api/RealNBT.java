package dev.arubik.realmcraft.Api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTDouble;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTLong;
import com.github.retrooper.packetevents.protocol.nbt.NBTShort;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import com.google.gson.JsonElement;

import dev.arubik.realmcraft.Api.RealCache.RealCacheMap;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Managers.Depend;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class RealNBT {

    private ItemStack item;
    private ItemStack itemStack;
    private RealCache<ItemMeta> metaCache = new RealCache<ItemMeta>(1200);
    private RealCache<NBTCompound> compoundCache = new RealCache<NBTCompound>(1200);
    private RealCache<List<String>> loreCache = new RealCache<List<String>>(1200);
    private static RealCacheMap<String, Component> ComponentCache = new RealCacheMap<String, Component>(1200);

    public int getEnchantmentLevel(String enchantment) {
        return getEnchantmentLevel(org.bukkit.enchantments.Enchantment.getByName(enchantment));
    }

    public Boolean containsAny(Collection<String> list) {
        for (String s : list) {
            if (contains(s))
                return true;
        }
        return false;
    }

    public ItemMeta getItemMeta() {
        ItemMeta metaT;
        Optional<ItemMeta> meta = metaCache.getOptional();
        if (meta.isPresent()) {
            metaT = meta.get();
        } else {
            metaT = itemStack.getItemMeta();
            metaCache.cache(metaT);
        }
        return metaT;
    }

    public int getEnchantmentLevel(Enchantment enchantment) {
        if (itemStack.getEnchantments().get(enchantment) != null)
            return itemStack.getEnchantmentLevel(enchantment);
        return 0;
    }

    public Map<String, Integer> getAdvancedEnchantments() {
        Map<String, Integer> enchants = new java.util.HashMap<String, Integer>();
        for (Enchantment enchant : itemStack.getEnchantments().keySet()) {
            enchants.put(enchant.getKey().toString(), itemStack.getEnchantmentLevel(enchant));
        }
        return enchants;
    }

    public String getId() {
        return item.getType().name();
    }

    public String getType() {

        return "MINECRAFT";
    }

    public Material getMaterial() {
        return itemStack.getType();
    }

    public RealNBT(final ItemStack realItem) {
        this.item = realItem;
        this.itemStack = realItem.clone();
        updateCache();
    }

    public static RealNBT fromItemStack(ItemStack item) {
        return new RealNBT(item);
    }

    public static enum AllowedTypes {
        String, Integer, Double, Float, Long, Short, Byte, Boolean, JsonElement, NBTTag, OBJECT, LIST, Compound;

        <T> T getValue(NBTTag tag) {
            return (T) tag.getValue();
        }

        public static AllowedTypes Prediction(String key) {
            switch (key) {
                case "MMOITEMS_ATTACK_DAMAGE": {

                    return AllowedTypes.Double;
                }
                default: {
                    return null;
                }
            }
        }

        public AllowedTypes basic() {
            if (this == AllowedTypes.String) {
                return AllowedTypes.String;
            } else if (this == AllowedTypes.Integer
                    || this == AllowedTypes.Double
                    || this == AllowedTypes.Float
                    || this == AllowedTypes.Long
                    || this == AllowedTypes.Short
                    || this == AllowedTypes.Byte
                    || this == AllowedTypes.Boolean) {
                return AllowedTypes.Integer;
            } else if (this == AllowedTypes.JsonElement
                    || this == AllowedTypes.NBTTag
                    || this == AllowedTypes.OBJECT) {
                return AllowedTypes.Compound;
            } else if (this == AllowedTypes.LIST) {
                return AllowedTypes.LIST;
            }
            return this;
        }

        public static AllowedTypes fromString(String name) {
            String upper = name.toUpperCase();
            upper = upper.replace("TAG_", "");
            switch (upper) {
                case "STRING":
                    return String;
                case "INT":
                    return Integer;
                case "DOUBLE":
                    return Double;
                case "FLOAT":
                    return Float;
                case "LONG":
                    return Long;
                case "SHORT":
                    return Short;
                case "BYTE":
                    return Byte;
                case "BOOLEAN":
                    return Boolean;
                case "JSONELEMENT":
                    return JsonElement;
                case "NBT":
                    return NBTTag;
                case "LIST":
                    return LIST;
                default:
                    return AllowedTypes.OBJECT;
            }

        }

        public static AllowedTypes fromNBTType(NBTType<?> type) {
            if (type == NBTType.INT) {
                return Integer;
            }
            if (type == NBTType.STRING) {
                return String;
            }
            if (type == NBTType.DOUBLE) {
                return Double;
            }
            if (type == NBTType.FLOAT) {
                return Float;
            }
            if (type == NBTType.LONG) {
                return Long;
            }
            if (type == NBTType.BYTE) {
                return Byte;
            }
            if (type == NBTType.SHORT) {
                return Short;
            }
            if (type == NBTType.LIST) {
                return LIST;
            }
            return AllowedTypes.OBJECT;
        }
    }

    public static class KeyedTag implements Keyed {

        NamespacedKey key;

        public KeyedTag(NBTTag originalTag) {
            key = new NamespacedKey(originalTag.getKey(), originalTag.getValue().toString());
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return key;
        }

    }

    public static class NBTTag {
        private String key;
        private AllowedTypes type;
        private Object value;

        public KeyedTag toKeyed() {
            return new KeyedTag(this);
        }

        public NBTTag(String key, AllowedTypes type, Object value) {
            this.key = key;
            this.type = type;
            switch (type) {
                case String:
                    this.value = value.toString();
                    break;
                case Integer:
                    this.value = Integer.parseInt(value.toString());
                    break;
                case Double:
                    this.value = Double.parseDouble(value.toString());
                    break;
                case Float:
                    this.value = Float.parseFloat(value.toString());
                    break;
                case Long:
                    this.value = Long.parseLong(value.toString());
                    break;
                case Short:
                    this.value = Short.parseShort(value.toString());
                    break;
                case Byte:
                    this.value = Byte.parseByte(value.toString());
                    break;
                case Boolean:
                    this.value = Boolean.parseBoolean(value.toString());
                    break;
                case JsonElement:
                    this.value = (JsonElement) value;
                    break;
                case NBTTag:
                    this.value = (NBTTag) value;
                    break;
                case LIST:
                    this.value = value;

                default:
                    this.value = value;
                    break;
            }

            if (type == AllowedTypes.LIST) {
                NBTList list = (NBTList) value;
                if (list.getTag(0).getType() == NBTType.COMPOUND) {

                } else {
                    this.value = list.getTags();
                }
            }

        }

        public String getKey() {
            return key;
        }

        public AllowedTypes getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }

        public JsonElement toJson() {
            return new JsonBuilder().append("key", key).append("type", type.toString()).append("value", value).toJson();
        }

        public NBT toNBT() {
            // TODO
            return null;
        }

    }

    public <T> void setInternal(String key, Object value, Class<T> type) {
        NBTCompound compound = getNBTCompound();

        switch (type.getSimpleName()) {
            case "String":
                compound.setTag(key, new NBTString(value.toString()));
                break;
            case "int":
                compound.setTag(key, new NBTInt((int) value));
                break;
            case "Integer":
                compound.setTag(key, new NBTInt((int) value));
                break;
            case "Double":
                compound.setTag(key, new NBTDouble((double) value));
                break;
            case "Float":
                compound.setTag(key, new NBTFloat((float) value));
                break;
            case "Long":
                compound.setTag(key, new NBTLong((long) value));
                break;
            case "Short":
                compound.setTag(key, new NBTShort((short) value));
                break;
            case "Byte":
                compound.setTag(key, new NBTByte((byte) value));
                break;
            case "Boolean":
                compound.setTag(key, new NBTByte((byte) value));
                break;
            case "JsonElement": {
                /*
                 * // get as list of NbtTags
                 * try {
                 * 
                 * List<NBTTag> tags = (List<NBTTag>) value;
                 * List<NbtBase<String>> list = new ArrayList<NbtBase<String>>();
                 * for (NBTTag tag : tags) {
                 * list.add(tag.toNBT());
                 * }
                 * compound.put(key, list);
                 * } catch (Throwable e) {
                 * 
                 * }
                 */
            }
            default:
                break;
        }
        com.github.retrooper.packetevents.protocol.item.ItemStack item = SpigotConversionUtil
                .fromBukkitItemStack(itemStack);
        item.setNBT(compound);
        itemStack = SpigotConversionUtil.toBukkitItemStack(item);

        updateCache();
    }

    protected void updateCache() {
        if (itemStack == null)
            return;
        if (itemStack.getType().isAir())
            return;
        compoundCache.cache(SpigotConversionUtil.fromBukkitItemStack(itemStack).getNBT());
        metaCache.cache(itemStack.getItemMeta());
    }

    public <T> T getInternal(String key, Class<T> type) {
        NBTCompound compound = getNBTCompound();

        switch (type.getSimpleName()) {
            case "String":
                return (T) compound.getStringTagOrThrow(key).getValue();
            case "int":
                return (T) (Integer) compound.getNumberTagOrThrow(key).getAsInt();
            case "Integer":
                return (T) (Integer) compound.getNumberTagOrThrow(key).getAsInt();
            case "Double":
                return (T) (Double) compound.getNumberTagOrThrow(key).getAsDouble();
            case "Float":
                return (T) (Float) compound.getNumberTagOrThrow(key).getAsFloat();
            case "Long":
                return (T) (Long) compound.getNumberTagOrThrow(key).getAsLong();
            case "Short":
                return (T) (Short) compound.getNumberTagOrThrow(key).getAsShort();
            case "Byte":
                return (T) (Byte) compound.getNumberTagOrThrow(key).getAsByte();
            case "Boolean":
                Byte val = compound.getNumberTagOrThrow(key).getAsByte();
                return (T) (Boolean) (val == 1);
        }
        return null;
    }

    public Set<String> getKeys() {
        NBTCompound compound = getNBTCompound();
        if (compound == null)
            return Set.of();
        return compound.getTagNames();
    }

    public <T> T get(String key, Class<T> type, T def) {
        T value = getInternal(key, type);
        if (value == null) {
            value = def;
        }
        return value;
    }

    public void put(NBTTag tag) {
        switch (tag.getType()) {
            case String:
                setString(tag.getKey(), (String) tag.getValue());
                break;
            case Integer:
                setInt(tag.getKey(), (Integer) tag.getValue());
                break;
            case Double:
                setDouble(tag.getKey(), (Double) tag.getValue());
                break;
            case Float:
                setFloat(tag.getKey(), (Float) tag.getValue());
                break;
            case Long:
                setLong(tag.getKey(), (Long) tag.getValue());
                break;
            case Short:
                setShort(tag.getKey(), (Short) tag.getValue());
                break;
            case Byte:
                setByte(tag.getKey(), (Byte) tag.getValue());
                break;
            case Boolean:
                setBoolean(tag.getKey(), (Boolean) tag.getValue());
                break;
            case JsonElement:
                setJsonElement(tag.getKey(), (JsonElement) tag.getValue());
                break;
            case LIST:
                setInternal(tag.getKey(), tag.getValue(), JsonElement.class);
                break;
            default:
                setInternal(tag.getKey(), tag.getValue(), tag.getValue().getClass());
                break;

        }
    }

    public NBTTag get(String key) {
        NBTCompound compound = getNBTCompound();
        if (key.equalsIgnoreCase("Enchantments")) {
            Map<String, Integer> enchants = getAdvancedEnchantments();
            // serialize to string
            JsonBuilder builder = new JsonBuilder();
            for (String enchant : enchants.keySet()) {
                builder.append(enchant, enchants.get(enchant));
            }
            return new NBTTag(key, AllowedTypes.String, builder.toString());
        }
        AllowedTypes type = AllowedTypes.fromNBTType(compound.getTags().get(key).getType());
        if (AllowedTypes.Prediction(key) != null) {
            type = AllowedTypes.Prediction(key);
        }
        Object val = null;
        switch (type.basic()) {
            case String:
                val = compound.getStringTagValueOrThrow(key);
                break;
            case Integer:
                val = compound.getNumberTagOrThrow(key).getAsNumber();
                break;
            case Compound:
                val = compound.getCompoundTagOrThrow(key);
                break;
            case LIST:
                val = compound.getCompoundListTagOrThrow(key);
                break;
        }
        return new NBTTag(key, type, val);
    }

    public Boolean contains(String key) {
        return hasTag(key);
    }

    public Boolean hasTag(String key) {
        if (itemStack == null)
            return false;
        if (itemStack.getType() == Material.AIR)
            return false;
        if (itemStack.getItemMeta() == null)
            return false;
        NBTCompound compound = getNBTCompound();
        if (compound == null)
            return false;
        return compound.getTagNames().contains(key);
    }

    protected NBTCompound getNBTCompound() {

        NBTCompound compound;

        if (compoundCache.isCached()) {
            try {
                compound = compoundCache.get();
            } catch (Exception e) {
                compound = SpigotConversionUtil.fromBukkitItemStack(itemStack).getNBT();
            }
        } else {
            compound = SpigotConversionUtil.fromBukkitItemStack(itemStack).getNBT();
            compoundCache.cache(compound);
        }

        return compound;
    }

    public void setString(String key, String value) {
        setInternal(key, value, String.class);
    }

    public String getString(String key, String def) {
        return get(key, String.class, def);
    }

    public String getString(String key) {
        return getInternal(key, String.class);
    }

    public void setInt(String key, int value) {
        setInternal(key, value, int.class);
    }

    public int getInt(String key, int def) {
        return get(key, int.class, def);
    }

    public int getInt(String key) {
        return getInternal(key, int.class);
    }

    public void setInteger(String key, Integer value) {
        setInternal(key, value, Integer.class);
    }

    public Integer getInteger(String key, Integer def) {
        return get(key, Integer.class, def);
    }

    public Integer getInteger(String key) {
        return getInternal(key, Integer.class);
    }

    public void setDouble(String key, Double value) {
        setInternal(key, value, Double.class);
    }

    public Double getDouble(String key, Double def) {
        return get(key, Double.class, def);
    }

    public Double getDouble(String key) {
        return getInternal(key, Double.class);
    }

    public void setFloat(String key, Float value) {
        setInternal(key, value, Float.class);
    }

    public Float getFloat(String key, Float def) {
        return get(key, Float.class, def);
    }

    public Float getFloat(String key) {
        return getInternal(key, Float.class);
    }

    public void setLong(String key, Long value) {
        setInternal(key, value, Long.class);
    }

    public Long getLong(String key, Long def) {
        return get(key, Long.class, def);
    }

    public Long getLong(String key) {
        return getInternal(key, Long.class);
    }

    public void setShort(String key, Short value) {
        setInternal(key, value, Short.class);
    }

    public Short getShort(String key, Short def) {
        return get(key, Short.class, def);
    }

    public Short getShort(String key) {
        return getInternal(key, Short.class);
    }

    public void setByte(String key, Byte value) {
        setInternal(key, value, Byte.class);
    }

    public Byte getByte(String key, Byte def) {
        return get(key, Byte.class, def);
    }

    public Byte getByte(String key) {
        return getInternal(key, Byte.class);
    }

    public void setBoolean(String key, Boolean value) {
        // parse boolean to byte
        Byte b = (byte) (value ? 1 : 0);
        setInternal(key, b, Byte.class);
    }

    public Boolean getBoolean(String key, Boolean def) {
        // get byte and parse to boolean
        Byte b = get(key, Byte.class, (byte) (def ? 1 : 0));
        return b == 1;
    }

    public Boolean getBoolean(String key) {
        return getInternal(key, Boolean.class);
    }

    public void setJsonElement(String key, JsonElement value) {
        setInternal(key, value, JsonElement.class);
    }

    public JsonElement getJsonElement(String key, JsonElement def) {
        return get(key, JsonElement.class, def);
    }

    public JsonBuilder getJsonElement(String key) {
        return getInternal(key, JsonBuilder.class);
    }

    public void remove(String key) {
        NBTCompound compound = getNBTCompound();

        compound.removeTag(key);

        com.github.retrooper.packetevents.protocol.item.ItemStack item = SpigotConversionUtil
                .fromBukkitItemStack(itemStack);
        item.setNBT(compound);
        itemStack = SpigotConversionUtil.toBukkitItemStack(item);
        updateCache();
    }

    public static final MiniMessage miniMessage = MiniMessage.builder().build();
    public static final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.legacySection();

    public void setLore(List<String> lore) {
        loreCache.cache(lore);
        List<Component> newLore = lore.stream()
                .map(line -> ComponentCache.getOptional(line).orElseGet(() -> {
                    Component component = miniMessage.deserialize(line);
                    ComponentCache.set(line, component);
                    return component;
                }))
                .collect(Collectors.toCollection(ArrayList::new));

        this.itemStack.lore(newLore);
        updateCache();
    }

    public List<String> getLore() {
        if (loreCache.isCached()) {
            try {
                return loreCache.get();
            } catch (Exception e) {
                e.printStackTrace(); // Manejar la excepción adecuadamente o imprimir un mensaje de registro.
            }
        }

        if (this.itemStack.lore() == null)
            return new ArrayList<>();

        return this.itemStack.lore().stream()
                .map(line -> miniMessage.serialize(line))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> serializeLore(List<Component> loreComponents) {
        return loreComponents.stream()
                .map(line -> miniMessage.serialize(line))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Component> deserializeLore(List<String> loreStrings) {
        return loreStrings.stream()
                .map(line -> ComponentCache.getOptional(line).orElseGet(() -> {
                    Component component = miniMessage.deserialize(line);
                    ComponentCache.set(line, component);
                    return component;
                }))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void putLoreLine(String line, LorePosition alignment) {
        List<String> lore = new ArrayList<>(getLore());
        switch (alignment) {
            case TOP:
                lore.add(0, line);
                break;
            case MID:
                lore.add(lore.size() / 2, line);
                break;
            case BOTTOM:
                lore.add(line);
                break;
        }
        setLore(lore);
    }

    public void putLoreLines(List<String> lines, LorePosition alignment) {
        List<String> lore = new ArrayList<>(getLore());
        switch (alignment) {
            case TOP:
                lore.addAll(0, lines);
                break;
            case MID:
                lore.addAll(lore.size() / 2, lines);
                break;
            case BOTTOM:
                lore.addAll(lines);
                break;
            case REPLACE:
                for (int i = 0; i < lore.size(); ++i) {
                    String s = lore.get(i);
                    if (Objects.equals(s, alignment.replace)) {
                        lore.addAll(i, lines);
                        break;
                    }
                }
                break;
        }
        setLore(lore);
    }

    public void setDisplayName(String name) {
        ItemMeta item = this.itemStack.getItemMeta();

        Component component;
        if (ComponentCache.getOptional(name).isPresent()) {
            component = ComponentCache.getOptional(name).get();
        } else {
            component = miniMessage.deserialize(name);
            ComponentCache.set(name, component);
        }
        item.displayName(component);
        this.itemStack.setItemMeta(item);
        updateCache();
    }

    public String getSimpleName() {
        if (!this.getItemMeta().hasDisplayName()) {
            return ChatColor.stripColor(this.itemStack.getI18NDisplayName());
        }
        return ChatColor.stripColor(this.getItemMeta().getDisplayName());
    }

    public String getDisplayName() {
        return (String) miniMessage.serialize(this.itemStack.getItemMeta().displayName());
    }

    public String getPlaceholderName() {
        return "%langutils_material_" + this.itemStack.getType().toString() + "%";
    }

    public Boolean hasDisplayName() {
        return this.getItemMeta().hasDisplayName();
    }

    public Boolean hasLore() {
        return this.getItemMeta().hasLore();
    }

    public Boolean hasEnchantments() {
        return this.getItemMeta().hasEnchants();
    }

    public static final ItemStack Empty = new ItemStack(Material.AIR);

    public static List<String> EmptyList() {
        return new ArrayList<>();
    }

    public ItemStack take(int amount) {
        if (itemStack.getAmount() - amount <= 0) {
            return RealNBT.Empty;
        }
        itemStack.setAmount(itemStack.getAmount() - amount);

        return itemStack;

    }

    public ItemStack[] getItemStackArray(String key) {
        return Utils.itemStackArrayFromBase64(getString(key));
    }

    public void setPlaceholderApi(Player player) {
        List<String> lore = getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, PlaceholderAPI.setPlaceholders((OfflinePlayer) player, lore.get(i)));
        }
        setLore(lore);
    }

    public void replaceLoreSection(String regex, String replace) {
        List<String> lore = getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains(regex)) {
                lore.set(i, lore.get(i).replace(regex, replace));
            }
        }
        setLore(lore);
    }

    public ItemStack getOriginalItem() {
        return item;
    }

    public ItemStack getItemStack() {
        if (getLore().size() == 0) {
            itemStack.editMeta(meta -> meta.lore(null));
        }
        return itemStack;
    }

    public void setEnchantments(Map<Enchantment, Integer> enchants) {

        // remove all enchants
        itemStack.editMeta(meta -> {
            for (Enchantment enchant : itemStack.getEnchantments().keySet()) {
                meta.removeEnchant(enchant);
            }
        });

        itemStack.editMeta(meta -> {
            for (Enchantment enchant : enchants.keySet()) {
                meta.addEnchant(enchant, enchants.get(enchant), true);
            }
        });
    }

    public ItemStack regenerate(Player player) {
        if (contains("MMOITEMS_ITEM_ID") && contains("MMOITEMS_ITEM_TYPE")) {

            if (Depend.isPluginEnabled("MMOItems")) {
                List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                        .map(Type::getId).collect(Collectors.toList());
                Object type = getString("MMOITEMS_ITEM_TYPE");
                if (types.contains(type)) {
                    type = MMOItems.plugin.getTypes().get(type.toString());
                    String id = getString("MMOITEMS_ITEM_ID");

                    return MMOItems.plugin.getItem((Type) type, id,
                            PlayerData.get(player));
                }
            }

        }

        return itemStack;
    }

    public ItemStack regenerate(Player player, Double unidentifier) {
        if (contains("MMOITEMS_ITEM_ID") && contains("MMOITEMS_ITEM_TYPE")) {

            if (Depend.isPluginEnabled("MMOItems")) {
                List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                        .map(Type::getId).collect(Collectors.toList());
                Object type = getString("MMOITEMS_ITEM_TYPE");
                if (types.contains(type)) {
                    Type typee = MMOItems.plugin.getTypes().get(type.toString());
                    String id = getString("MMOITEMS_ITEM_ID");
                    ItemStack a = MMOItems.plugin.getItem(typee, id,
                            PlayerData.get(player));
                    if (Utils.Chance(unidentifier, 100)) {
                        NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(a);
                        a = typee.getUnidentifiedTemplate().newBuilder(nbtItem).build();
                    }
                    return a;
                }
            }

        }

        return itemStack;
    }

    public String cleanDump() {
        return ChatColor.stripColor(dumpIgnoringTags("HSTRY_NAME", "CustomModelData", "display", "MMOITEMS_LORE"));
    }

    public String dump() {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder = jsonBuilder.append("Type", getType());
        jsonBuilder = jsonBuilder.append("Material", itemStack.getType().toString());
        jsonBuilder = jsonBuilder.append("Amount", itemStack.getAmount());
        jsonBuilder = jsonBuilder.append("CustomModelData", itemStack.getItemMeta().hasCustomModelData() == false ? 0
                : itemStack.getItemMeta().getCustomModelData());

        getNBTCompound();

        for (String key : getKeys()) {

            if (key.equals("CustomModelData"))
                continue;

            jsonBuilder = jsonBuilder.append(get(key));
        }

        String display = JsonBuilder.create().append("Name", itemStack.getItemMeta().hasDisplayName() == false ? ""
                : itemStack.getItemMeta().getDisplayName())
                .append("Lore", itemStack.getItemMeta().hasLore() == false ? new ArrayList<>()
                        : itemStack.getItemMeta().getLore())
                .toString();

        jsonBuilder = jsonBuilder.append("Display", display);

        return jsonBuilder.toString();

    }

    @Override
    public String toString() {
        return dump();
    }

    public String dumpIgnoringTags(String... tags) {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder = jsonBuilder.append("Type", getType());
        jsonBuilder = jsonBuilder.append("Material", itemStack.getType().toString());
        jsonBuilder = jsonBuilder.append("Amount", itemStack.getAmount());

        if (!Arrays.asList(tags).contains("CustomModelData")) {

            jsonBuilder = jsonBuilder.append("CustomModelData",
                    itemStack.getItemMeta().hasCustomModelData() == false ? 0
                            : itemStack.getItemMeta().getCustomModelData());
        }

        NBTCompound compound = getNBTCompound();

        for (String key : getKeys()) {

            if (key.equals("CustomModelData"))
                continue;

            if (Arrays.asList(tags).contains(key))
                continue;

            jsonBuilder = jsonBuilder.append(get(key));
        }

        if (!Arrays.asList(tags).contains("Display")) {

            String display = JsonBuilder.create().append("Name", itemStack.getItemMeta().hasDisplayName() == false ? ""
                    : itemStack.getItemMeta().getDisplayName())
                    .append("Lore", itemStack.getItemMeta().hasLore() == false ? new ArrayList<>()
                            : itemStack.getItemMeta().getLore())
                    .toString();

            jsonBuilder = jsonBuilder.append("Display", display);
        }

        return jsonBuilder.toString();

    }

    public String dumpWithColor(String[]... patterns) {
        String dump = dump();
        for (String[] pattern : patterns) {
            dump = dump.replace(pattern[0], pattern[1]);
        }
        return dump;
    }

    public RealNBT addItemFlags(ItemFlag... itemFlags) {
        itemStack.editMeta(meta -> {
            meta.addItemFlags(itemFlags);
        });
        return this;
    }

    public RealNBT addPotionEffect(RealPotionEffect effect) {
        ItemMeta a = itemStack.getItemMeta();
        if (a instanceof PotionMeta potion) {
            potion.addCustomEffect(new PotionEffect(effect.type, effect.duration, effect.level), true);
            itemStack.setItemMeta(potion);
        }
        return this;
    }

    public RealNBT setColor(int red, int green, int blue) {
        ItemMeta a = itemStack.getItemMeta();
        Color col = Color.fromRGB(red, green, blue);
        if (a instanceof PotionMeta newmeta) {
            newmeta.setColor(col);
        }
        if (a instanceof LeatherArmorMeta newmeta) {
            newmeta.setColor(col);
        }
        if (a instanceof MapMeta newmeta) {
            newmeta.setColor(col);
        }
        return this;
    }
    // Item
    // Enchantments
    // EntityTag
    // display
    // AttributeModifiers
    // Unbreakable
    // HideFlags
    // CanDestroy
    // PickupDelay
    // All
    // title
    // author
    // pages
    // Fireworks

    public static Set<String> NBT_VANILLA_TAG = new HashSet<>(Arrays.asList("CustomModelData", "Item", "Enchantments",
            "EntityTag", "display", "AttributeModifiers", "Unbreakable", "HideFlags", "CanDestroy", "PickupDelay",
            "All", "title", "author", "pages", "Fireworks"));

    public RealNBT removedInvisibleNBT() {
        getKeys().forEach(key -> {
            if (NBT_VANILLA_TAG.contains(key))
                return;
            remove(key);
        });
        return this;
    }

    public void editMeta(Consumer<ItemMeta> consumer) {
        itemStack.editMeta(consumer);
        updateCache();
    }

    public <M extends ItemMeta> void editMeta(Class<M> metaClass, Consumer<? super M> consume) {
        itemStack.editMeta(metaClass, consume);
        updateCache();
    }

    public int getAmount() {
        return itemStack.getAmount();
    }

    public void repair() {
        if (this.getItemMeta() instanceof Damageable) {
            this.editMeta(Damageable.class, damageable -> {
                damageable.setDamage(0);
            });
        }
        if (this.contains("MMOITEMS_DURABILITY")) {
            this.setInteger("MMOITEMS_DURABILITY", this.getInteger("MMOITEMS_MAX_DURABILITY"));
        }

    }

    public boolean damage(int damage) {
        if (this.contains("MMOITEMS_MAX_DURABILITY")) {
            int durability = this.getInteger("MMOITEMS_MAX_DURABILITY");
            if (this.contains("MMOITEMS_DURABILITY")) {
                durability = this.getInteger("MMOITEMS_DURABILITY");
            }
            durability -= damage;
            if (durability < 0) {
                return false;
            }
            this.setInteger("MMOITEMS_DURABILITY", durability);
            Double ddur = (double) durability;
            this.editMeta(Damageable.class, (Damageable meta) -> {
                int maxVanillDurability = item.getType().getMaxDurability();
                // get what percent is durability of max durability
                double percent = (double) ddur / (double) this.getInt("MMOITEMS_MAX_DURABILITY");
                // get the new durability
                int newVanillaDurability = (int) (maxVanillDurability * percent);
                // set the new durability
                meta.setDamage(maxVanillDurability - newVanillaDurability);
            });
        } else {
            if (this.getItemMeta() instanceof Damageable damageable) {

            }
        }
        return true;
    }

    public String prettyPrint() {
        return this.getAmount() + "x "
                + this.getSimpleName() + " [" + this.dump() + "]";
    }
}
