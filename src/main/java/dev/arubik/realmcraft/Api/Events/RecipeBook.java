package dev.arubik.realmcraft.Api.Events;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;

import dev.arubik.realmcraft.Managers.Depend;

public class RecipeBook implements Depend {

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "ProtocolLib" };
    }

    @SuppressWarnings({ "deprecation", "unused" })
    @EventHandler
    public void onPlayerAutoRecipe(PlayerRecipeBookClickEvent e) {
        // get recipe items from bukkit server
        Player p = e.getPlayer();
        Recipe ee = Bukkit.getRecipe(e.getRecipe());

        if ((p.getOpenInventory().getTopInventory() instanceof CraftingInventory) == false)
            return;
        if (ee instanceof ShapedRecipe sre) {
            ShapedRecipe sr = (ShapedRecipe) ee;
            CraftingInventory inv = (CraftingInventory) p.getOpenInventory().getTopInventory();
            Map<Character, ItemStack> ingredients = sr.getIngredientMap();
            int rowindex = 0;
            for (String row : sr.getShape()) {
                for (char c : row.toCharArray()) {
                    ItemStack item = ingredients.get(c);
                    if (item != null) {
                        int colindex = row.indexOf(c);
                        int relativeslot = rowindex * 3 + colindex;
                        if (p.getInventory().containsAtLeast(item, item.getAmount())) {
                            p.getInventory().removeItem(item);
                            inv.setItem(relativeslot, item);
                        }
                    }
                }
                rowindex++;
            }
        }
    }

}
