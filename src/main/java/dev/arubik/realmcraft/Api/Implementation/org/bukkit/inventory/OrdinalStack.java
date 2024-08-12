package dev.arubik.realmcraft.Api.Implementation.org.bukkit.inventory;

import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Super Interface to the Basic ItemStack, adding utilities functions.
 */
public abstract class OrdinalStack extends org.bukkit.inventory.ItemStack {

    // Ordinal start
    /**
     * Check if the item is dyable.
     *
     * @return true if is dyable
     */
    @NotNull
    public Boolean dyable() {

        return false;
    }

    /**
     * Check if the item is dyable.
     *
     * <p>
     * Plugins should check that {@link #dyable()} returns <code>true</code> before
     * calling this method.
     * </p>
     * 
     * @param red   {@link java.lang.Integer}
     * @param green {@link java.lang.Integer}
     * @param blue  {@link java.lang.Integer}
     * 
     * @throws Exception "Not dyable item"
     */
    public void dye(Integer red, Integer green, Integer blue) {
        Color col = Color.fromRGB(red, green, blue);
        ItemMeta meta = this.getItemMeta();
        if (meta instanceof PotionMeta newmeta) {
            newmeta.setColor(col);
            this.setItemMeta(newmeta);
        } else if (meta instanceof LeatherArmorMeta newmeta) {
            newmeta.setColor(col);
            this.setItemMeta(newmeta);
        } else if (meta instanceof MapMeta newmeta) {
            newmeta.setColor(col);
            this.setItemMeta(newmeta);
        } else {
            new Exception("Not dyable item");
        }
    }
}
