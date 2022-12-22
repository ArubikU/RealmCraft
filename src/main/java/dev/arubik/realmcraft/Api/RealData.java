package dev.arubik.realmcraft.Api;

import java.util.List;

import org.bukkit.attribute.AttributeModifier.Operation;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.RealNBT.NBTTag;

public class RealData {
    public static final String REALM_NAMESPACE = "realmcraft";
    public String name;
    public boolean isLocalizedName;
    public List<String> lore;
    public List<RealAttribute> attributes;
    public List<RealEnchantment> enchantments;
    public List<RealFlag> flags;
    public List<NBTTag> nbt;

    public class RealAttribute {
        public String name;
        public double value;
        public @NotNull Operation operation;
        public String slot;
    }

    public class RealEnchantment {
        public String name;
        public int level;
    }

    public class RealFlag {
        public String name;
        public boolean value;
    }
}
