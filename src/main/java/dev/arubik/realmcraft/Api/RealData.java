package dev.arubik.realmcraft.Api;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import dev.arubik.realmcraft.Api.RealNBT.AllowedTypes;
import dev.arubik.realmcraft.Api.RealNBT.NBTTag;

public class RealData {
    public static final String REALM_NAMESPACE = "realmcraft";
    public String name;
    public boolean isLocalizedName;
    public boolean ignoreNamespaceAndId;
    public List<String> lore;
    public List<RealAttribute> attributes;
    public List<RealEnchantment> enchantments;
    public List<RealFlag> flags;
    public List<NBTTag> nbt;
    public List<RealPotionEffect> effects;
    public Integer[] color;

    public static List<RealEnchantment> EnchantmentFromList(List<String> list) {
        List<RealEnchantment> enchantments = new ArrayList<>();
        for (String enchantment : list) {
            String[] split = enchantment.split(":");
            RealEnchantment realEnchantment = new RealEnchantment();
            realEnchantment.name = split[0];
            realEnchantment.level = Integer.parseInt(split[1]);
            enchantments.add(realEnchantment);
        }
        return enchantments;
    }

    public static List<RealAttribute> AttributeFromList(List<String> list) {
        List<RealAttribute> attributes = new ArrayList<>();
        for (String attribute : list) {
            String[] split = attribute.split(":");
            RealAttribute realAttribute = new RealAttribute();
            realAttribute.name = split[0];
            realAttribute.value = Double.parseDouble(split[1]);
            realAttribute.operation = Operation.valueOf(split[2]);
            realAttribute.slot = split[3];
            attributes.add(realAttribute);
        }
        return attributes;
    }

    public static List<RealFlag> FlagFromList(List<String> list) {
        List<RealFlag> flags = new ArrayList<>();
        for (String flag : list) {
            String[] split = flag.split(":");
            RealFlag realFlag = new RealFlag();
            realFlag.name = split[0];
            realFlag.value = Boolean.parseBoolean(split[1]);
            flags.add(realFlag);
        }
        return flags;
    }

    public static List<NBTTag> NBTFromList(List<String> list) {
        List<NBTTag> nbt = new ArrayList<>();
        for (String tag : list) {
            String[] split = tag.split(":");
            NBTTag nbtTag = new NBTTag(split[0], AllowedTypes.valueOf(split[1]), split[2]);
            nbt.add(nbtTag);
        }
        return nbt;
    }

    public static List<RealPotionEffect> PotionEffectFromList(List<String> list) {
        List<RealPotionEffect> potions = new ArrayList<>();
        for (String tag : list) {
            String[] split = tag.split(" ");
            RealPotionEffect effect = new RealPotionEffect();
            PotionEffectType a = PotionEffectType.getByName(split[0]);
            if (a != null) {
                effect.type = a;
                if (split.length > 0) {
                    int b = Integer.valueOf(split[1]);
                    effect.level = b;
                }
                if (split.length > 1) {
                    int b = Integer.valueOf(split[2]);
                    effect.duration = b;
                }
                potions.add(effect);
            }
        }
        return potions;
    }
}
