package dev.arubik.realmcraft.Api;

import org.bukkit.Material;

public class RealBlock extends RealStack {

    public static enum BlockType {
        MUSHROOM, WIRE, NOTEBLOCK, CHORUSFRUIT, LEAVES, FIRE
    }

    public RealBlock(String id, String namespace, int modelid, Material material, RealData data) {
        super(id, namespace, modelid, material, data);
    }

}
