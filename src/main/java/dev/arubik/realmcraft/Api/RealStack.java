package dev.arubik.realmcraft.Api;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.Events.BuildEvent;
import dev.arubik.realmcraft.Api.RealData.RealAttribute;
import dev.arubik.realmcraft.Api.RealData.RealEnchantment;
import dev.arubik.realmcraft.Api.RealData.RealFlag;
import dev.arubik.realmcraft.Api.RealNBT.AllowedTypes;
import dev.arubik.realmcraft.Api.RealNBT.NBTTag;
import dev.arubik.realmcraft.Handlers.JsonBuilder;

public class RealStack {
    private String id;
    private String namespace;
    private int modelid;
    private Material material;
    private RealData data;

    public RealStack(String id, String namespace, int modelid, Material material, RealData data) {
        this.id = id;
        this.namespace = namespace;
        this.modelid = modelid;
        this.material = material;
        this.data = data;
    }

    public ItemStack buildItemStack() {
        ItemStack item = new ItemStack(material);
        item.getItemMeta().setCustomModelData(modelid);
        if (data.isLocalizedName) {
            item.getItemMeta().setLocalizedName(id);
        } else {
            item.getItemMeta().setDisplayName(id);
        }
        for (RealEnchantment enchantment : data.enchantments) {
            item.addUnsafeEnchantment(Enchantment.getByName(enchantment.name), enchantment.level);
        }
        for (RealAttribute attribute : data.attributes) {
            item.getItemMeta().addAttributeModifier(Attribute.valueOf(attribute.name),
                    new org.bukkit.attribute.AttributeModifier(attribute.name, attribute.value, attribute.operation));
        }
        for (RealFlag flag : data.flags) {
            if (flag.value) {
                item.getItemMeta().addItemFlags(ItemFlag.valueOf(flag.name));
            }
        }
        RealNBT nbt = new RealNBT(item);
        JsonBuilder json = new JsonBuilder();
        json.append("id", id);
        json.append("namespace", namespace);
        NBTTag tag = new NBTTag("realmcraft", AllowedTypes.JsonElement, json.toJson());
        nbt.put(tag);
        for (NBTTag tag2 : data.nbt) {
            nbt.put(tag2);
        }
        BuildEvent event = new BuildEvent(nbt, id, namespace);
        event.CallEvent();
        return nbt.getItemStack();
    }

}
