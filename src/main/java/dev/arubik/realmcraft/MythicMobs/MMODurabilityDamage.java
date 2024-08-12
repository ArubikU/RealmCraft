package dev.arubik.realmcraft.MythicMobs;

import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;

public class MMODurabilityDamage implements ITargetedEntitySkill, Depend {

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MythicMobs", "MMOItems" };
    }

    int durabilityDamage = 0;
    String slot = "HAND";
    String item = "ALL";

    public MMODurabilityDamage(MythicLineConfig config) {
        this.durabilityDamage = config.getInteger(new String[] { "durabilitydamage", "durabilitydmg", "durability" },
                0);
        this.slot = config.getString(new String[] { "slot" }, "HAND");
        this.item = config.getString(new String[] { "item" }, "ALL");
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        LivingEntity LivingMob = (LivingEntity) BukkitAdapter.adapt(target);
        if (slot == "ALL" || slot == "*") {
            if (LivingMob instanceof Item item) {
                ItemStack itemStack = item.getItemStack();
                RealNBT nbt = new RealNBT(itemStack);
                if (nbt.contains("MMOITEMS_MAX_DURABILITY")) {
                    int maxDurability = nbt.getInt("MMOITEMS_MAX_DURABILITY");
                    int durability = nbt.getInt("MMOITEMS_DURABILITY", maxDurability);
                    if (durability == 0) {
                        durability = maxDurability;
                    }

                    if (durability - durabilityDamage <= 0) {
                        item.remove();
                    } else {
                        nbt.setInt("MMOITEMS_DURABILITY", durability - durabilityDamage);
                        itemStack = nbt.getItemStack();
                        if (itemStack.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
                            int original = itemStack.getType().getMaxDurability();
                            double percent = (double) (durability - durabilityDamage) / (double) maxDurability;
                            int newDurability = (int) (original * percent);
                            ((org.bukkit.inventory.meta.Damageable) itemStack.getItemMeta())
                                    .setDamage(original - newDurability);
                        }
                        item.setItemStack(itemStack);
                    }
                }
            }

        }
        if (LivingMob.getEquipment().getItem(EquipmentSlot.valueOf(slot)) != null) {
            ItemStack item = LivingMob.getEquipment().getItem(EquipmentSlot.valueOf(slot));
            RealNBT nbt = new RealNBT(item);
            if (nbt.contains("MMOITEMS_MAX_DURABILITY")) {
                int maxDurability = nbt.getInt("MMOITEMS_MAX_DURABILITY");
                int durability = nbt.getInt("MMOITEMS_DURABILITY", maxDurability);
                if (durability == 0) {
                    durability = maxDurability;
                }

                if (durability - durabilityDamage <= 0) {
                    if (LivingMob instanceof org.bukkit.entity.Player player) {
                        // play break sound
                    }
                    LivingMob.getEquipment().setItem(EquipmentSlot.valueOf(slot), null);
                } else {
                    nbt.setInt("MMOITEMS_DURABILITY", durability - durabilityDamage);
                    item = nbt.getItemStack();
                    if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
                        int original = item.getType().getMaxDurability();
                        double percent = (double) (durability - durabilityDamage) / (double) maxDurability;
                        int newDurability = (int) (original * percent);
                        ((org.bukkit.inventory.meta.Damageable) item.getItemMeta()).setDamage(original - newDurability);
                    }
                    if (LivingMob instanceof org.bukkit.entity.Player player && EquipmentSlot.valueOf(slot).isHand()) {
                        player.swingHand(EquipmentSlot.valueOf(slot));
                    }
                    LivingMob.getEquipment().setItem(EquipmentSlot.valueOf(slot), item);
                }
            }
        }

        return null;

    }
}
