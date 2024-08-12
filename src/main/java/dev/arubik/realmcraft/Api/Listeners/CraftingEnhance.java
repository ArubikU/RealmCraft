package dev.arubik.realmcraft.Api.Listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.willfp.ecoenchants.enchants.EcoEnchant;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Managers.Depend;

public class CraftingEnhance implements Listener {

  private static final String MMOITEMS_DURABILITY = "MMOITEMS_DURABILITY";
  private static final String CRAFTING_REMAIN = "CRAFT";
  private static final String MMOITEMS_MAX_DURABILITY = "MMOITEMS_MAX_DURABILITY";

  Map<UUID, NamespacedKey> crafting = new HashMap<>();
  Map<UUID, ItemStack[]> craftingMatrix = new HashMap<>();
  Map<String, Boolean> fastIgnore = new HashMap<>();

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPreItemCraft(org.bukkit.event.inventory.PrepareItemCraftEvent event) {
    if (event.getRecipe() == null)
      return;

    CraftingRecipe recipe = (CraftingRecipe) event.getRecipe();
    UUID uuidP = event.getView().getPlayer().getUniqueId();
    if (recipe.getKey().getKey().equalsIgnoreCase("minecraft")) {
      return;
    }
    crafting.put(uuidP, recipe.getKey());
    craftingMatrix.put(uuidP, event.getInventory().getMatrix());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onItemCraft(org.bukkit.event.inventory.CraftItemEvent event) {
    // get inputs

    if (event.getRecipe() == null)
      return;
    CraftingRecipe recipe = (CraftingRecipe) event.getRecipe();
    UUID uuidP = event.getView().getPlayer().getUniqueId();
    if (crafting.get(uuidP) == null)
      return;
    if (fastIgnore.containsKey(recipe.getKey().asString())) {
      if (fastIgnore.get(recipe.getKey().asString())) {
        return;
      }
    }
    if (!crafting.get(uuidP).equals(recipe.getKey()))
      return;

    boolean edit = false;

    ItemStack[] inputs = craftingMatrix.get(uuidP);
    ItemStack[] result = event.getInventory().getMatrix().clone();

    int uses = 1;
    for (int i = 0; i < inputs.length; i++) {
      ItemStack inputStack = inputs[i];
      if (inputStack == null)
        continue;
      RealNBT ToolNBT = new RealNBT(inputStack);
      result[i] = null;
      if (ToolNBT.contains(CRAFTING_REMAIN)) {
        if (ToolNBT.contains(MMOITEMS_MAX_DURABILITY)
            && ToolNBT.getString(CRAFTING_REMAIN).equalsIgnoreCase("DUR")) {

          Integer durability = ToolNBT.getInt(MMOITEMS_MAX_DURABILITY);
          if (ToolNBT.contains(MMOITEMS_DURABILITY))
            durability = ToolNBT.getInt(MMOITEMS_DURABILITY, durability);
          // get unbreaking enchantment level
          Integer unbreaking = ToolNBT.getEnchantmentLevel(Enchantment.DURABILITY);
          // calculate chance to break
          double chance = unbreaking * 15;
          // random number
          Random random = new Random();
          // if random number is less than chance to break, break
          while (uses > 0) {
            uses--;
            if ((random.nextInt(100) > chance) || unbreaking == 0) {
              durability--;
            }
          }
          ToolNBT.setInt(MMOITEMS_DURABILITY, durability);
          // e.getBow().setItemMeta(ToolNBT.getItemMeta());
          if (durability <= 0) {
            if (Depend.isPluginEnabled("EcoEnchants")) {
              if (EcoEnchant.getByName("UNUSING") != null) {
                if (ToolNBT.getEnchantmentLevel(EcoEnchant.getByName("UNUSING")) > 0) {

                  event.setCancelled(true);
                  break;
                }
              }
            }
            continue;
          } else {

            ItemStack item = ToolNBT.getItemStack();
            // set durability of item base on the durability of the item
            final double ddmage = durability;
            item.editMeta(Damageable.class, (Damageable meta) -> {
              int maxVanillDurability = item.getType().getMaxDurability();
              // get what percent is durability of max durability
              double percent = (double) ddmage / (double) ToolNBT.getInt("MMOITEMS_MAX_DURABILITY");
              // get the new durability
              int newVanillaDurability = (int) (maxVanillDurability * percent);
              // set the new durability
              meta.setDamage(maxVanillDurability - newVanillaDurability);

            });
            result[i] = (item);
            edit = true;
          }

        }
        if (ToolNBT.getString(CRAFTING_REMAIN).equalsIgnoreCase("ITEM")) {

        }
      }
    }

    if (edit == true) {

      if (!fastIgnore.containsKey(recipe.getKey().asString())) {
        fastIgnore.put(recipe.getKey().asString(), false);
      }
      // paste the result matrix over the "event.getInventory().getMatrix().clone()"
      // matrix ignoring the air
      ItemStack[] matrix = event.getInventory().getMatrix();
      for (int i = 0; i < matrix.length; i++) {
        if (result[i] != null) {
          matrix[i] = result[i];
        }
      }
      event.getInventory().setMatrix(matrix);
    }
  }

  public static void register() {
    Bukkit.getPluginManager().registerEvents(new CraftingEnhance(), realmcraft.getInstance());
  }

}
