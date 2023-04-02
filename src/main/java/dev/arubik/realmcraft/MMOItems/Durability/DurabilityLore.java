package dev.arubik.realmcraft.MMOItems.Durability;

import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.DynamicLoreLine;
import dev.arubik.realmcraft.Api.EmptyDynamicLine;
import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.LorePosition;
import dev.arubik.realmcraft.Api.RealLore;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Events.LoreEvent;

public class DurabilityLore implements RealLore {
    class DurabilityModifier implements ItemBuildModifier {
        @Override
        public ItemStack modifyItem(Player player, ItemStack item) {
            RealNBT nbt = new RealNBT(item);
            if (nbt.contains("MMOITEMS_DURABILITY")) {
                // make unbreakable and hide unbreakable
                ItemMeta meta = item.getItemMeta();
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                item.setItemMeta(meta);
            }
            return item;
        }

        @Override
        public Boolean able(ItemStack item) {
            RealNBT nbt = new RealNBT(item);
            return nbt.contains("MMOITEMS_MAX_DURABILITY");
        };
    }

    class DurabilityLoreLine implements DynamicLoreLine {
        private String line;

        public DurabilityLoreLine(String line) {
            this.line = line;
        }

        @Override
        public String parseLine(Player player, ItemStack item) {
            RealNBT nbt = new RealNBT(item);
            Integer maxDurability = nbt.getInteger("MMOITEMS_MAX_DURABILITY");
            Integer durability = maxDurability;
            if (nbt.contains("MMOITEMS_DURABILITY"))
                durability = nbt.getInteger("MMOITEMS_DURABILITY");
            return line.replace("{durability}", durability.toString()).replace("{max_durability}",
                    maxDurability.toString());
        }
    }

    public static void register() {
        DurabilityLore durabilityLore = new DurabilityLore();
        LoreEvent.addLore(durabilityLore);
        LoreEvent.addItemBuildModifier(durabilityLore.new DurabilityModifier());
    }

    private List<DynamicLoreLine> lore = List.of(new EmptyDynamicLine(),
            new DurabilityLoreLine(realmcraft.getMinecraftLang().getString("lore.durability",
                    "<white>Durabilidad: {durability} / {max_durability}")));

    private LorePosition position = LorePosition.BOTTOM;

    @Override
    public Boolean able(ItemStack item) {
        RealNBT nbt = new RealNBT(item);
        return nbt.contains("MMOITEMS_MAX_DURABILITY");
    }

    @Override
    public List<DynamicLoreLine> getLoreLines() {
        return lore;
    }

    @Override
    public LorePosition getLorePosition() {
        return position;
    };

}
