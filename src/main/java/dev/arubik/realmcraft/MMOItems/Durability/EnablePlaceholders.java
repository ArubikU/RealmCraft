package dev.arubik.realmcraft.MMOItems.Durability;

import org.bukkit.Material;

import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Module;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.StringStat;

public class EnablePlaceholders extends BooleanStat implements Module {

    public EnablePlaceholders(String id, Material mat, String name, String[] lore, String[] types,
            Material[] materials) {
        super(id, mat, name, lore, types, materials);
    }

    public EnablePlaceholders() {
        super("PLACEHOLDERS_ENABLED", Material.ANVIL, "Enable Placeholders", new String[] {}, new String[] { "all" });
    }

    @Override
    public void register() {
        MMOItems.plugin.getStats().register("PLACEHOLDERS_ENABLED", this);
    }

    @Override
    public String configId() {
        return "placeholder-lore";
    }

    @Override
    public String displayName() {
        return "Placeholder Lore Stat";
    }
}