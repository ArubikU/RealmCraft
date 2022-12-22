package dev.arubik.realmcraft.Api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.google.gson.JsonElement;

import dev.arubik.realmcraft.realmcraft;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class RealNBT {
    private ItemStack item;
    private ItemStack itemStack;

    public RealNBT(ItemStack item) {
        this.item = item;
        this.itemStack = item;
    }

    public static RealNBT fromItemStack(ItemStack item) {
        return new RealNBT(item);
    }

    public ItemStack getOriginalItem() {
        return item;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static enum AllowedTypes {
        String, Integer, Double, Float, Long, Short, Byte, Boolean, JsonElement, NBTTag
    }

    public static class NBTTag {
        private String key;
        private AllowedTypes type;
        private Object value;

        public NBTTag(String key, AllowedTypes type, Object value) {
            this.key = key;
            this.type = type;
            this.value = value;
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
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(itemStack));
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
                NbtCompound jsonCompound = NbtFactory.ofCompound(key);
                JsonElement jsonElement = (JsonElement) value;
                jsonElement.getAsJsonObject().entrySet().forEach(entry -> {
                    jsonCompound.put(entry.getKey(), entry.getValue().getAsString());
                });
                compound.put(jsonCompound);
            }
            default:
                compound.putObject(key, value);
                break;
        }
        NbtFactory.setItemTag(itemStack, compound);
    }

    public <T> T getInternal(String key, Class<T> type) {
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(itemStack));
        NbtBase wrapper = compound.getValue(key);
        if (wrapper == null) {
            return null;
        }
        switch (type.getSimpleName()) {
            case "String":
                return (T) wrapper.getValue();
            case "int":
                return (T) (Integer) wrapper.getValue();
            case "Integer":
                return (T) (Integer) wrapper.getValue();
            case "Double":
                return (T) (Double) wrapper.getValue();
            case "Float":
                return (T) (Float) wrapper.getValue();
            case "Long":
                return (T) (Long) wrapper.getValue();
            case "Short":
                return (T) (Short) wrapper.getValue();
            case "Byte":
                return (T) (Byte) wrapper.getValue();
            case "Boolean":
                return (T) (Boolean) wrapper.getValue();
            case "JsonElement": {
                val jsonElement = (JsonElement) wrapper.getValue();
                return (T) jsonElement;
            }
            default:
                return (T) wrapper.getValue();
        }
    }

    public <T> T get(String key, Class<T> type, T def) {
        T value = getInternal(key, type);
        if (value == null) {
            value = def;
        }
        return value;
    }

    public void put(NBTTag tag) {
        setInternal(tag.getKey(), tag.getValue(), tag.getValue().getClass());
    }

    public NBTTag get(String key) {
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(itemStack));
        NbtBase wrapper = compound.getValue(key);
        if (wrapper == null) {
            return null;
        }
        return new NBTTag(key, AllowedTypes.valueOf(wrapper.getType().name()), wrapper.getValue());
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
        setInternal(key, value, Boolean.class);
    }

    public Boolean getBoolean(String key, Boolean def) {
        return get(key, Boolean.class, def);
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

    public JsonElement getJsonElement(String key) {
        return getInternal(key, JsonElement.class);
    }

    public void remove(String key) {
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(itemStack));
        compound.remove(key);
        NbtFactory.setItemTag(itemStack, compound);
    }

    public static enum Alignment {
        TOP,
        MID,
        BOTTOM
    }

    public void setLore(List<String> lore) {
        List<String> newLore = new ArrayList<>();
        for (String line : lore) {
            newLore.add(
                    realmcraft.getLegacyComponentSerializer().serialize(realmcraft.getMiniMessage().deserialize(line)));
        }
    }

    public List<String> getLore() {
        List<String> lore = new ArrayList<>();
        for (String line : itemStack.getItemMeta().getLore()) {
            lore.add(
                    realmcraft.getMiniMessage().serialize(realmcraft.getLegacyComponentSerializer().deserialize(line)));
        }
        return lore;
    }

    public void putLoreLine(String line, Alignment alignment) {
        List<String> lore = getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
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

}
