package dev.arubik.realmcraft.Api;

import org.bukkit.attribute.AttributeModifier.Operation;
import org.jetbrains.annotations.NotNull;

public class RealAttribute {
    public String name;
    public double value;
    public @NotNull Operation operation;
    public String slot;
}