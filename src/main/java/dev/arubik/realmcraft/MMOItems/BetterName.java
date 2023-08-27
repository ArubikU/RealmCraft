package dev.arubik.realmcraft.MMOItems;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.MMOItems.Range.NBTCompund;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentSerializer;

public class BetterName extends StringStat {

    public BetterName(String id, Material mat, String name, String[] lore, String[] types, Material[] materials) {
        super(id, mat, name, lore, types, materials);

    }

    public BetterName() {
        super("NAME_CUSTOM", Material.PAPER, "Set the custom name with full adventure", new String[] {},
                new String[] { "all" });
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        Component comp = MiniMessage.miniMessage().deserialize(data.getString());
        item.getMeta().displayName(comp);
        item.getItemStack().editMeta(meta -> {
            meta.displayName(comp);

        });
    }

    public static void register() {
        RealMessage.Found("MMOItems found starting register of BName");
        MMOItems.plugin.getStats().register("NAME_CUSTOM", new BetterName());
    }
}
