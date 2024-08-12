package dev.arubik.realmcraft.MMOItems;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Handlers.RealMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeathLore extends StringListStat implements GemStoneStat {
    public DeathLore() {
        super("DEATH_LORE", VersionMaterial.WRITABLE_BOOK.toMaterial(), "DeathLore",
                new String[] { "The item lore." }, new String[] { "all" });
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringListData whenInitialized(Object object) {
        if (!(object instanceof List<?>)) {
            RealMessage.sendConsoleMessage("DeathLore: Must specify a string list");
        }
        return new StringListData((List<String>) object);
    }
}
