package dev.arubik.realmcraft.MMOItems.Durability;

import org.bukkit.Material;

import dev.arubik.realmcraft.Handlers.RealMessage;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.type.StringStat;

public class RepairMaterial extends StringStat {

    public RepairMaterial(String id, Material mat, String name, String[] lore, String[] types, Material[] materials) {
        super(id, mat, name, lore, types, materials);
    }

    public RepairMaterial() {
        super("REPAIR_MATERIAL", Material.ANVIL, "Repair Material ID", new String[] {}, new String[] { "all" });
    }

    public static void register() {
        MMOItems.plugin.getStats().register("REPAIR_MATERIAL", new RepairMaterial());
        RealMessage.sendConsoleMessage("<yellow>Repair Listener ON!");
    }
}
