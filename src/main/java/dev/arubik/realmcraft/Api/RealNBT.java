package dev.arubik.realmcraft.Api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.gson.JsonElement;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class RealNBT {
    private ItemStack item;
    private ItemStack itemStack;
    private RealCache<ItemMeta> metaCache = new RealCache<ItemMeta>(1200);

    public int getEnchantmentLevel(String enchantment) {
        return getEnchantmentLevel(org.bukkit.enchantments.Enchantment.getByName(enchantment));
    }

    public ItemMeta getItemMeta() {
        if (metaCache.isCached()) {
            return metaCache.forcedGet();
        }
        ItemMeta meta = itemStack.getItemMeta();
        metaCache.set(meta);
        return meta;
    }

    public int getEnchantmentLevel(Enchantment enchantment) {
        if (itemStack.getEnchantments().get(enchantment) != null)
            return itemStack.getEnchantmentLevel(enchantment);
        return 0;
    }

    public Map<String, Integer> getAdvancedEnchantments() {
        Map<String, Integer> enchants = new java.util.HashMap<String, Integer>();

        return enchants;
    }

    public String getId() {
        return item.getType().name();
    }

    public String getType() {
        return "MINECRAFT";
    }

    public RealNBT(final ItemStack realItem) {
        this.item = realItem;
        this.itemStack = realItem;
    }

    public static RealNBT fromItemStack(ItemStack item) {
        return new RealNBT(item);

    }

    public static enum AllowedTypes {
        String, Integer, Double, Float, Long, Short, Byte, Boolean, JsonElement, NBTTag, OBJECT;

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
                default:
                    return AllowedTypes.OBJECT;
            }

        }
    }

    public static class NBTTag {
        private String key;
        private AllowedTypes type;
        private Object value;

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
                default:
                    break;
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
    }

    public <T> void setInternal(String key, Object value, Class<T> type) {
        NbtCompound compound = NbtFactory
                .asCompound(NbtFactory.fromItemTag(MinecraftReflection.getBukkitItemStack(itemStack)));
        switch (type.getSimpleName()) {
            case "String":
                compound.put(key, (String) value);
                break;
            case "int":
                compound.put(key, (int) value);
                break;
            case "Integer":
                compound.put(key, (Integer) value);
                break;
            case "Double":
                compound.put(key, (Double) value);
                break;
            case "Float":
                compound.put(key, (Float) value);
                break;
            case "Long":
                compound.put(key, (Long) value);
                break;
            case "Short":
                compound.put(key, (Short) value);
                break;
            case "Byte":
                compound.put(key, (Byte) value);
                break;
            case "Boolean":
                compound.putObject(key, value);
                break;
            case "JsonElement": {
                JsonElement jsonElement = (JsonElement) value;
                NbtCompound jsonCompound = NbtFactory.ofCompound(jsonElement.getAsString());
                compound.put(jsonCompound);
            }
            default:
                compound.putObject(key, value);
                break;
        }
        ItemStack item = MinecraftReflection.getBukkitItemStack(itemStack);
        NbtFactory.setItemTag((ItemStack) item, compound);
        itemStack = item;
    }

    public <T> T getInternal(String key, Class<T> type) {
        NbtCompound compound = NbtFactory
                .asCompound(NbtFactory.fromItemTag(MinecraftReflection.getBukkitItemStack(itemStack)));
        NbtBase wrapper = compound.getValue(key);
        if (wrapper == null) {
            return null;
        }
        switch (type.getSimpleName()) {
            case "JsonBuilder": {
                NbtCompound jsonElement = (NbtCompound) wrapper;
                JsonBuilder jsonOutput = new JsonBuilder();
                for (String key2 : jsonElement.getKeys()) {
                    jsonOutput.append(key2, jsonElement.getValue(key2).getValue());
                }
                return (T) jsonOutput;
            }
            default:
                return (T) wrapper.getValue();
        }
    }

    public Set<String> getKeys() {
        NbtCompound compound = NbtFactory
                .asCompound(NbtFactory.fromItemTag(MinecraftReflection.getBukkitItemStack(itemStack)));
        return compound.getKeys();
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
            default:
                setInternal(tag.getKey(), tag.getValue(), tag.getValue().getClass());
                break;

        }
    }

    public NBTTag get(String key) {
        NbtCompound compound = NbtFactory
                .asCompound(NbtFactory.fromItemTag(MinecraftReflection.getBukkitItemStack(itemStack)));
        NbtBase wrapper = compound.getValue(key);
        if (wrapper == null) {
            return null;
        }
        return new NBTTag(key, AllowedTypes.fromString(wrapper.getType().name()), wrapper.getValue());
    }

    public Boolean contains(String key) {
        if (itemStack == null)
            return false;
        if (itemStack.getType() == Material.AIR)
            return false;
        if (itemStack.getItemMeta() == null)
            return false;

        NbtCompound compound = NbtFactory
                .asCompound(NbtFactory.fromItemTag(MinecraftReflection.getBukkitItemStack(itemStack)));
        return compound.containsKey(key);
    }

    public Boolean hasTag(String key) {
        if (itemStack == null)
            return false;
        if (itemStack.getType() == Material.AIR)
            return false;
        if (itemStack.getItemMeta() == null)
            return false;

        NbtCompound compound = NbtFactory
                .asCompound(NbtFactory.fromItemTag(MinecraftReflection.getBukkitItemStack(itemStack)));
        return compound.containsKey(key);
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
        NbtCompound compound = NbtFactory
                .asCompound(NbtFactory.fromItemTag(MinecraftReflection.getBukkitItemStack(itemStack)));
        compound.remove(key);
        NbtFactory.setItemTag(itemStack, compound);

    }

    public void setLore(List<String> lore) {
        ArrayList<String> newLore = new ArrayList<String>();
        for (String line : lore) {
            newLore.add(LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage().deserialize(line)));
        }
        this.itemStack.setLore(newLore);
    }

    public List<String> getLore() {
        ArrayList<String> lore = new ArrayList<String>();
        if (!this.itemStack.getItemMeta().hasLore()) {
            return lore;
        }
        for (String line : this.itemStack.getItemMeta().getLore()) {
            lore.add((String) MiniMessage.miniMessage()
                    .serialize(LegacyComponentSerializer.legacySection().deserialize(line)));
        }
        return lore;
    }

    public void putLoreLine(String line, LorePosition alignment) {
        List<String> lore = this.getLore();
        if (lore == null) {
            lore = new ArrayList<String>();
        }
        switch (alignment) {
            case TOP: {
                lore.add(0, line);
                break;
            }
            case MID: {
                lore.add(lore.size() / 2, line);
                break;
            }
            case BOTTOM: {
                lore.add(line);
            }
        }
        this.setLore(lore);
    }

    public void putLoreLines(List<String> lines, LorePosition alignment) {
        List<String> lore = this.getLore();
        if (lore == null) {
            lore = new ArrayList<String>();
        }
        switch (alignment) {
            case TOP: {
                for (int i = 0; i < lines.size(); ++i) {
                    lore.add(i, lines.get(i));
                }
                break;
            }
            case MID: {
                for (int i = 0; i < lines.size(); ++i) {
                    lore.add(lore.size() / 2 + i, lines.get(i));
                }
                break;
            }
            case BOTTOM: {
                for (int i = 0; i < lines.size(); ++i) {
                    lore.add(lines.get(i));
                }
                break;
            }
            case REPLACE: {
                for (int i = 0; i < lore.size(); ++i) {
                    String s = lore.get(i);
                    if (s != alignment.replace)
                        continue;
                    for (int a = 0; a < lines.size(); ++a) {
                        lore.add(lines.get(a));
                    }
                    break;
                }
                break;
            }
        }
        this.setLore(lore);
    }

    public void setDisplayName(String name) {
        ItemMeta item = this.itemStack.getItemMeta();
        item.setDisplayNameComponent(
                BungeeComponentSerializer.get().serialize(MiniMessage.miniMessage().deserialize(name)));
        item.setDisplayName(LegacyComponentSerializer.legacySection()
                .serialize(MiniMessage.miniMessage().deserialize(name)));
        this.itemStack.setItemMeta(item);
    }

    public String getDisplayName() {
        return (String) MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacySection()
                .deserialize(this.itemStack.getItemMeta().getDisplayName()));
    }

    public Boolean hasDisplayName() {
        return this.itemStack.getItemMeta().hasDisplayName();
    }

    public Boolean hasLore() {
        return this.itemStack.getItemMeta().hasLore();
    }

    public Boolean hasEnchantments() {
        return itemStack.getItemMeta().hasEnchants();
    }

    public static final ItemStack Empty = new ItemStack(Material.AIR);

    public static List<String> EmptyList() {
        return new ArrayList<>();
    }

    public RealNBT take1() {
        itemStack.setAmount(itemStack.getAmount() - 1);
        return this;
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
        // verify if lore is empty to remove it
        if (itemStack.getItemMeta().hasLore()) {
            if (itemStack.getItemMeta().getLore().size() == 0) {
                ItemMeta item = itemStack.getItemMeta();
                item.setLore(null);
                itemStack.setItemMeta(item);
            }
        }

        if (itemStack.getItemMeta().hasDisplayName()) {
            if (itemStack.getItemMeta().getDisplayName().isEmpty()
                    || itemStack.getItemMeta().getDisplayNameComponent().length == 0) {
                ItemMeta item = itemStack.getItemMeta();
                item.setDisplayName(null);
                itemStack.setItemMeta(item);
            }
        }
        return itemStack;
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

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(itemStack.getType().name()).append(" ");
        for (String key : getKeys()) {
            sb.append(" ").append(key).append(" : ").append(get(key)).append("  ").append(get(key).getClass().getName())
                    .append(" ");
        }
        return sb.toString();

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

}
