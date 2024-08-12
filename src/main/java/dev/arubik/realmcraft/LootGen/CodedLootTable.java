package dev.arubik.realmcraft.LootGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Managers.Depend;
import lombok.Setter;
import net.Indyuce.mmoitems.MMOItems;

public class CodedLootTable {
  private int min_items;
  private int max_items;
  private List<CodedItem> items = new ArrayList<>();

  public CodedLootTable(int min_items, int max_items) {
    this.min_items = min_items;
    this.max_items = max_items;
  }

  public void addItem(CodedItem item) {
    items.add(item);
  }

  public void addItem(ItemStack item, int weight, int min_amount, int max_amount) {
    CodedItem codedItem = new CodedVanillaItem(item, weight, min_amount, max_amount);
    items.add(codedItem);
  }

  public void addItem(ItemStack item, int weight, int min_amount, int max_amount, int chance) {
    CodedItem codedItem = new CodedVanillaItem(item, weight, min_amount, max_amount);
    codedItem.chance = chance;
    items.add(codedItem);
  }

  public List<ItemStack> generateLoot() {
    List<ItemStack> loot = new ArrayList<>();
    Random random = new Random();
    int numItems = random.nextInt((max_items - min_items) + 1) + min_items;

    for (int i = 0; i < numItems; i++) {
      CodedItem codedItem = getRandomWeightedItem();
      if (codedItem != null) {

        int amount = random.nextInt((codedItem.max_amount - codedItem.min_amount) + 1) + codedItem.min_amount;
        ItemStack item = codedItem.getItem().clone();
        item.setAmount(amount);
        item = applyRandomEnchantments(item, codedItem, random);
        loot.add(item);
      }
    }

    return loot;
  }

  private CodedItem getRandomWeightedItem() {

    List<CodedItem> formatedItems = new ArrayList<>();
    for (CodedItem item : items) {
      if (item.chance < 100) {
        if (Utils.Chance(item.chance, 100)) {
          formatedItems.add(item);
        }
      } else {
        formatedItems.add(item);
      }
    }
    List<CodedItem> weightedItems = new ArrayList<>();
    for (CodedItem item : formatedItems) {
      for (int i = 0; i < item.weight; i++) {
        weightedItems.add(item);
      }
    }

    if (!weightedItems.isEmpty()) {
      return weightedItems.get(new Random().nextInt(weightedItems.size()));
    }

    return null;
  }

  private ItemStack applyRandomEnchantments(ItemStack item, CodedItem codedItem, Random random) {
    int numEnchants = random.nextInt((codedItem.max_enchants - codedItem.min_enchants) + 1)
        + codedItem.min_enchants;

    List<Enchantment> possibleEnchants = new ArrayList<>();
    for (String enchantName : codedItem.Enchants.keySet()) {
      Enchantment enchantment = Enchantment.getByName(enchantName);
      if (enchantment != null) {
        possibleEnchants.add(enchantment);
      }
    }

    for (int i = 0; i < numEnchants && !possibleEnchants.isEmpty(); i++) {
      Enchantment enchantment = possibleEnchants.remove(random.nextInt(possibleEnchants.size()));
      String levels = codedItem.Enchants.get(enchantment.getName());
      int maxLevel = Integer
          .parseInt(levels.split("to")[1].replace("#maxlevel#", String.valueOf(enchantment.getMaxLevel())));
      int level = random.nextInt(maxLevel) + 1;
      item.addEnchantment(enchantment, level);
    }

    return item;
  }

  public static class CodedItem {
    public ItemStack item = null;
    @Setter
    int weight;
    @Setter
    int min_amount;
    @Setter
    int chance;
    @Setter
    int max_amount;

    Map<String, String> Enchants = new HashMap<>();
    int min_enchants;
    int max_enchants;
    // FORMAT: DIG_SPEED, 1to#maxlevel#

    public CodedItem(int weight, int min_amount, int max_amount) {
      this.weight = weight;
      this.min_amount = min_amount;
      this.max_amount = max_amount;
      this.min_enchants = 0;
      this.max_enchants = 0;
      this.chance = 100;
    }

    public CodedItem(int weight, int min_amount, int max_amount, int chance) {
      this.weight = weight;
      this.min_amount = min_amount;
      this.max_amount = max_amount;
      this.min_enchants = 0;
      this.max_enchants = 0;
      this.chance = chance;
    }

    public void addEnchantment(String enchantment, String levels) {
      Enchants.put(enchantment, levels);
    }

    public void setEnchantRange(int min_enchants, int max_enchants) {
      this.min_enchants = min_enchants;
      this.max_enchants = max_enchants;
    }

    public ItemStack getItem() {
      return item;
    }
  }

  public static class CodedVanillaItem extends CodedItem {

    public CodedVanillaItem(ItemStack item) {
      super(1, 1, 1);
      this.item = item;
    }

    public CodedVanillaItem(ItemStack item, int weight, int min_amount, int max_amount) {
      super(weight, min_amount, max_amount);
      this.item = item;
    }

    public CodedVanillaItem(ItemStack item, int weight, int min_amount, int max_amount, int chance) {
      super(weight, min_amount, max_amount, chance);
      this.item = item;
    }
  }

  public static class CodedMMOItem extends CodedItem {

    protected String type = null;
    protected String id = null;

    @Override
    public ItemStack getItem() {
      if (item != null) {
        return item;
      }
      if (type != null && id != null) {
        if (MMOItems.plugin.getItem(type, id) != null) {
          this.item = MMOItems.plugin.getItem(type, id);
          return item;
        }
      }
      this.item = new ItemStack(Material.STONE);
      return item;
    }

    public CodedMMOItem(String type, String id) {
      super(1, 1, 1);
      if (Depend.isPluginEnabled("MMOItems")) {
        this.type = type;
        this.id = id;

      }
    }

    public CodedMMOItem(String type, String id, int weight, int min_amount, int max_amount) {
      super(weight, min_amount, max_amount);
      if (Depend.isPluginEnabled("MMOItems")) {
        this.type = type;
        this.id = id;

      }
    }

    public CodedMMOItem(String type, String id, int weight, int min_amount, int max_amount, int chance) {
      super(weight, min_amount, max_amount, chance);
      if (Depend.isPluginEnabled("MMOItems")) {
        this.type = type;
        this.id = id;

      }
    }
  }

}
