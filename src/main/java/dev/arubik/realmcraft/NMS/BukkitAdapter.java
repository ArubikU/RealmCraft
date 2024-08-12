package dev.arubik.realmcraft.NMS;

import javax.annotation.Nonnull;

import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;

import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BukkitAdapter {
  public static Entity toBukkit(net.minecraft.world.entity.Entity entity) {
    return entity.getBukkitEntity();
  }

  public static LivingEntity fromBukkit(@Nonnull org.bukkit.entity.LivingEntity entity) {
    return ((CraftLivingEntity) entity).getHandle();
  }

  public static BlockState fromBukkit(org.bukkit.block.Block block) {
    return ((CraftBlock) block).getNMS();
  }

  public static class BukkitAdapterBlock {
    public static boolean isContainer(BlockState block) {
      return block.getBlock() instanceof Container;
    }

    public static Container getContainer(BlockState block) {
      return (Container) block.getBlock();
    }
  }

  public static class BukkitAdapterItem {
    public static boolean isItemStack(org.bukkit.inventory.ItemStack item) {
      return item != null;
    }

    public static String getSimpleName(ItemStack item) {
      if (item.getDisplayName() != null) {
        return item.getDisplayName().getString();
      } else {
        return item.getItem().getName(item).getString();
      }
    }

    public static String prettyPrint(ItemStack item) {
      // return this.getAmount() + "x "
      // + this.getSimpleName() + " [" + this.dump() + "]";
      String tag = item.getTag() == null ? "null" : item.getTag().toString();
      return item.getCount() + "x " + getSimpleName(item) + " [" + tag + "]";
    }
  }
}
