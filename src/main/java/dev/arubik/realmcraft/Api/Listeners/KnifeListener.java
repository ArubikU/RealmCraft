package dev.arubik.realmcraft.Api.Listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.CooldownIntegration;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.LootGen.CodedLootTable;
import dev.arubik.realmcraft.LootGen.LootTable;

public class KnifeListener extends CooldownIntegration implements Listener {
  List<String> CropsIds = List.of("WHEAT", "CARROTS", "POTATOES", "BEETROOTS", "NETHER_WART");
  List<String> ToolIds = List.of("WOODEN_KNIFE", "STONE_KNIFE", "IRON_KNIFE", "GOLDEN_KNIFE", "DIAMOND_KNIFE",
      "NETHERITE_KNIFE");

  Map<Material, Object> replaceDrop = new HashMap<Material, Object>() {
    {
      put(Material.COBWEB, new ItemStack(Material.STRING));
      put(Material.SHORT_GRASS, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.WHEAT_SEEDS), 4, 1, 2, 100);
          addItem(new ItemStack(Material.WHEAT), 2, 1, 2, 50);
        }
      });
      put(Material.TALL_GRASS, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.WHEAT_SEEDS), 5, 1, 2, 77);
          addItem(new ItemStack(Material.WHEAT), 2, 1, 2, 75);
          addItem(new CodedMMOItem("CONSUMABLE", "CORN", 2, 1, 3, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "CUCUMBER", 2, 1, 3, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "lETTUCE", 2, 1, 3, 50));
        }
      });
      put(Material.FERN, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.WHEAT_SEEDS), 5, 1, 2, 77);
          addItem(new ItemStack(Material.WHEAT), 2, 1, 2, 75);
          addItem(new CodedMMOItem("CONSUMABLE", "PINEAPPLE", 2, 1, 3, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "STRAWBERRY", 2, 1, 3, 50));
        }
      });
      put(Material.DEAD_BUSH, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.WHEAT_SEEDS), 5, 1, 2, 77);
          addItem(new ItemStack(Material.WHEAT), 2, 1, 2, 75);
          addItem(new CodedMMOItem("CONSUMABLE", "CHILIPEPPER", 2, 1, 3, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "PEANUT", 2, 1, 3, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "HAZELNUT", 2, 1, 3, 50));
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
        }
      });
      put(Material.VINE, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.WHEAT_SEEDS), 4, 1, 2, 50);
          addItem(new ItemStack(Material.WHEAT), 2, 1, 2, 75);
          addItem(new CodedMMOItem("CONSUMABLE", "PURPLE_GRAPES", 2, 1, 3, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "GREEN_GRAPES", 2, 1, 3, 50));
        }
      });
      put(Material.OAK_LEAVES, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.GOLDEN_APPLE), 1, 0, 1, 1);
          addItem(new ItemStack(Material.APPLE), 2, 1, 1, 33);
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
          addItem(new ItemStack(Material.OAK_SAPLING), 2, 1, 2, 66);
        }
      });
      put(Material.BIRCH_LEAVES, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.GOLDEN_APPLE), 1, 0, 1, 1);
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
          addItem(new ItemStack(Material.BIRCH_SAPLING), 2, 1, 2, 66);
          addItem(new CodedMMOItem("CONSUMABLE", "PEAR", 2, 1, 2, 50));
        }
      });
      put(Material.CHERRY_LEAVES, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.GOLDEN_APPLE), 1, 0, 1, 1);
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
          addItem(new ItemStack(Material.CHERRY_SAPLING), 2, 1, 2, 66);
          addItem(new CodedMMOItem("CONSUMABLE", "CHERRY", 2, 1, 2, 50));
        }
      });
      put(Material.SPRUCE_LEAVES, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.GOLDEN_APPLE), 1, 0, 1, 1);
          addItem(new ItemStack(Material.APPLE), 2, 1, 1, 33);
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
          addItem(new ItemStack(Material.SPRUCE_SAPLING), 2, 1, 2, 66);
        }
      });
      put(Material.JUNGLE_LEAVES, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.GOLDEN_APPLE), 1, 0, 1, 1);
          addItem(new CodedMMOItem("CONSUMABLE", "BANANA", 2, 1, 2, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "MANGO", 2, 1, 2, 50));
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
          addItem(new ItemStack(Material.JUNGLE_SAPLING), 2, 1, 2, 66);
        }
      });
      put(Material.ACACIA_LEAVES, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.GOLDEN_APPLE), 1, 0, 1, 1);
          addItem(new CodedMMOItem("CONSUMABLE", "ORANGE", 2, 1, 2, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "LEMON", 2, 1, 2, 50));
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
          addItem(new ItemStack(Material.ACACIA_SAPLING), 2, 1, 2, 66);
        }
      });
      put(Material.DARK_OAK_LEAVES, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.GOLDEN_APPLE), 1, 0, 1, 1);
          addItem(new ItemStack(Material.APPLE), 2, 1, 1, 33);
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
          addItem(new ItemStack(Material.DARK_OAK_SAPLING), 2, 1, 2, 66);
        }
      });
      put(Material.AZALEA_LEAVES, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.GOLDEN_APPLE), 1, 0, 1, 1);
          addItem(new CodedMMOItem("CONSUMABLE", "CANNON", 2, 1, 2, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "DRAGON_FRUIT", 2, 1, 2, 50));
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
          addItem(new ItemStack(Material.AZALEA), 2, 1, 2, 66);
        }
      });
      put(Material.FLOWERING_AZALEA_LEAVES, new CodedLootTable(1, 2) {
        {
          addItem(new ItemStack(Material.GOLDEN_APPLE), 1, 0, 1, 1);
          addItem(new CodedMMOItem("CONSUMABLE", "CANNON", 2, 1, 2, 50));
          addItem(new CodedMMOItem("CONSUMABLE", "DRAGON_FRUIT", 2, 1, 2, 50));
          addItem(new ItemStack(Material.STICK), 3, 1, 4, 66);
          addItem(new ItemStack(Material.FLOWERING_AZALEA), 2, 1, 2, 66);
        }
      });
      // DEAD BUSH

      put(Material.OAK_SAPLING, new CodedLootTable(1, 1) {
        {
          addItem(new ItemStack(Material.DEAD_BUSH), 1, 1, 1, 100);
        }
      });
    }
  };

  public static void register() {
    Bukkit.getPluginManager().registerEvents(new KnifeListener(), realmcraft.getInstance());
  }

  // on right click with ToolIds if contains tag MMOITEMS_ITEM_ID and is eq to
  // ToolIds
  @EventHandler
  public void onRightClick(PlayerInteractEvent event) {
    if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK))
      return;
    // if (event.getHand() == EquipmentSlot.OFF_HAND)
    // return;
    if (event.getItem() == null)
      return;
    if (CropsIds.contains(event.getClickedBlock().getType().name())) {

      RealNBT nbt = new RealNBT(event.getItem());
      if (!nbt.contains("MMOITEMS_ITEM_ID"))
        return;
      if (!ToolIds.contains(nbt.getString("MMOITEMS_ITEM_ID")))
        return;
      // get drops from block
      // set block to stage 0
      if (event.getClickedBlock().getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
        if (!(ageable.getAge() == ageable.getMaximumAge()))
          return;
        ItemStack Lootstack = event.getItem().clone();
        if (Lootstack.getEnchantments().containsKey(Enchantment.LOOT_BONUS_MOBS)) {
          Lootstack.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS,
              Lootstack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS));
        }
        Collection<org.bukkit.inventory.ItemStack> drops = event.getClickedBlock().getDrops(event.getItem(),
            event.getPlayer());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(event.getClickedBlock(), event.getPlayer());
        Bukkit.getPluginManager().callEvent(blockBreakEvent);
        if (blockBreakEvent.isCancelled())
          return;
        Lootstack = null;
        ageable.setAge(1);
        event.getClickedBlock().setBlockData(ageable);
        // play block break sound
        event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(), "block.crop.break",
            1, 1);
        // update block to client
        event.getClickedBlock().getState().update();
        // verify enchantment telekinesis
        if (nbt.getEnchantmentLevel("TELEKINESIS") != 0) {
          for (org.bukkit.inventory.ItemStack drop : drops) {
            event.getPlayer().getInventory().addItem(drop);
          }
        } else {
          for (org.bukkit.inventory.ItemStack drop : drops) {
            event.getClickedBlock().getWorld()
                .dropItem(event.getClickedBlock().getLocation(), drop);
          }
        }

        if (!nbt.damage(1)) {
          event.getPlayer().getInventory().remove(event.getItem());
          event.getPlayer().playSound(event.getPlayer().getLocation(), "entity.item.break", 1, 1);
          event.getPlayer().swingHand(event.getHand());
        } else {
          event.getPlayer().getInventory().setItem(event.getHand(), nbt.getItemStack());
          event.getPlayer().swingHand(event.getHand());
        }

      }

    } else if (replaceDrop.containsKey(event.getClickedBlock().getType())) {
      if (!this.CooldownAvaliable(event.getPlayer()))
        return;
      RealNBT nbt = new RealNBT(event.getItem());
      if (!nbt.contains("MMOITEMS_ITEM_ID"))
        return;
      if (!ToolIds.contains(nbt.getString("MMOITEMS_ITEM_ID")))
        return;
      ItemStack[] drops = getDrop(event.getPlayer(), replaceDrop.get(event.getClickedBlock().getType()));
      BlockBreakEvent blockBreakEvent = new BlockBreakEvent(event.getClickedBlock(), event.getPlayer());
      Bukkit.getPluginManager().callEvent(blockBreakEvent);
      if (blockBreakEvent.isCancelled())
        return;
      this.CooldownStart(event.getPlayer());

      // play block break sound
      event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(),
          event.getClickedBlock().getBlockSoundGroup().getBreakSound(),
          1, 1);
      event.getClickedBlock().getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK,
          event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5,
          event.getClickedBlock().getBlockData());
      // remove block and play particles
      event.getClickedBlock().setType(Material.AIR);
      // verify enchantment telekinesis
      if (nbt.getEnchantmentLevel("TELEKINESIS") != 0) {
        for (org.bukkit.inventory.ItemStack drop : drops) {
          event.getPlayer().getInventory().addItem(drop);
        }
      } else {
        for (org.bukkit.inventory.ItemStack drop : drops) {
          event.getClickedBlock().getWorld()
              .dropItem(event.getClickedBlock().getLocation(), drop);
        }
      }

      if (!nbt.damage(1)) {
        event.getPlayer().getInventory().remove(event.getItem());
        event.getPlayer().playSound(event.getPlayer().getLocation(), "entity.item.break", 1, 1);
        event.getPlayer().swingHand(event.getHand());
      } else {
        event.getPlayer().getInventory().setItem(event.getHand(), nbt.getItemStack());
        event.getPlayer().swingHand(event.getHand());
      }
    }
  }

  public ItemStack[] getDrop(Player player, Object drop) {
    ItemStack[] drops = null;

    if (drop instanceof ItemStack) {
      drops = new ItemStack[] { (ItemStack) drop };
    } else if (drop instanceof ItemStack[]) {
      drops = (ItemStack[]) drop;
    } else if (drop instanceof CodedLootTable loot) {
      drops = loot.generateLoot().toArray(ItemStack[]::new);
    } else {
      String dropLine = (String) drop;
      if (dropLine.startsWith("lootTable:")) {
        dropLine = dropLine.replace("lootTable:", "");
        if (LootTable.exist(dropLine)) {
          drops = new LootTable(dropLine).genLoot(InventoryType.BARREL);
        }
      }
    }
    if (drops != null) {
      // remove null and air items
      drops = java.util.Arrays.stream(drops)
          .filter(item -> item != null && item.getType() != Material.AIR)
          .toArray(ItemStack[]::new);

    } else {
      drops = new ItemStack[0];
    }
    return drops;
  }
}
