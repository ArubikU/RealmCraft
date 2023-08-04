package dev.arubik.realmcraft.MMOItems.Durability;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.MMOCoreAPI;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreHook.MMOCoreRPGPlayer;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * When a consumable is eaten, restores stamina.
 *
 * @author Gunging
 */
public class RestoreStellium extends DoubleStat implements PlayerConsumable {
    public RestoreStellium() {
        super("RESTORE_STELLIUM", VersionMaterial.LIGHT_BLUE_DYE.toMaterial(), "Restore Stellium",
                new String[] { "The amount of stellium", "your consumable restores." }, new String[] { "consumable" });
    }

    @Override
    public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player, boolean vanillaEating) {

        // No data no service
        if (!mmo.hasData(this))
            return;

        // Get value
        DoubleData d = (DoubleData) mmo.getData(this);

        // Any stamina being provided?
        if (d.getValue() != 0) {
            MMOCore.plugin.dataProvider.getDataManager().get(player).giveStellium(d.getValue(), UpdateReason.COMMAND);
        }
    }

    public static void register() {
        MMOItems.plugin.getStats().register(new RestoreStellium());
    }

}
