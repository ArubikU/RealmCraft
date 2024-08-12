package dev.arubik.realmcraft.Api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.NMS.BukkitAdapter;
import dev.arubik.realmcraft.NMS.BukkitAdapter.BukkitAdapterBlock;
import dev.arubik.realmcraft.NMS.BukkitAdapter.BukkitAdapterItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.block.CustomBlock;

public class Targeter {

  public static Map<String, List<Entity>> Entities = new HashMap<String, List<Entity>>();
  public static List<Player> Players = new ArrayList<Player>();

  public static void preSetup(Player player) {
  }

  public static Player getTargetPlayer(final LivingEntity entity) {
    Entity e = BukkitAdapter
        .toBukkit(realmcraft.getInstance().getNMS().getTargetEntity(BukkitAdapter.fromBukkit(entity), 16));
    if (e instanceof Player) {
      return (Player) e;
    }
    return null;
  }

  public static Entity getTargetEntity(final LivingEntity entity) {
    return BukkitAdapter
        .toBukkit(realmcraft.getInstance().getNMS().getTargetEntity(BukkitAdapter.fromBukkit(entity), 16));
  }

  @Nullable
  public static CustomBlock getCustomBlock(Player player) {
    for (Block block : player.getLineOfSight(null, 6)) {
      if (MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData()).isPresent()) {
        return MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData()).get();
      }
    }
    return null;
  }

  @Nullable
  public static String getCustomBlockParsed(Player player) {
    for (Block block : player.getLineOfSight(null, 6)) {
      if (MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData()).isPresent()) {
        return MMOItems
            .getID(MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData()).get().getItem())
            + " LOC["
            + block.getX() + "," + block.getY() + "," + block.getZ() + "]";
      }
    }
    return null;
  }

  @Nullable
  public static String getBlock(Player player) {
    for (Block block : player.getLineOfSight(null, 6)) {
      if (block.getType() == Material.AIR)
        continue;

      if (BukkitAdapterBlock.isContainer(BukkitAdapter.fromBukkit(block))) {
        List<String> items = new ArrayList<>();
        BukkitAdapterBlock.getContainer(BukkitAdapter.fromBukkit(block)).getContents().forEach(item -> {
          items.add(BukkitAdapterItem.prettyPrint(item));
        });
        return block.getType().toString() + " LOC["
            + block.getLocation().getBlockX() + ","
            + block.getLocation().getBlockY() + ","
            + block.getLocation().getBlockZ() + "], INV[" + String.join(", ", items) + "]"

        ;
      }

      return block.getType().toString() + " LOC[" + block.getX() + "," + block.getY() + "," + block.getZ() + "] "
          +
          block.getBlockData().toString();
    }
    return null;
  }
}
