package dev.arubik.realmcraft.NMS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;

import com.google.common.collect.Lists;

import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class nms {
  CraftServer server;
  DedicatedServer dServer;
  DedicatedPlayerList dList;
  List<ServerLevel> worlds;

  public nms() {
    this.server = ((CraftServer) Bukkit.getServer());
    this.dList = this.server.getHandle();
    this.dServer = this.dList.getServer();
    this.worlds = Lists.newArrayList(this.dServer.getAllLevels());
  }

  @Nullable
  public ServerPlayer getPlayer(@Nonnull UUID uuid) {
    return dList.getPlayer(uuid);
  }

  @Nullable
  public Entity getEntity(@Nonnull UUID uuid) {
    for (ServerLevel world : worlds) {
      if (world.getEntity(uuid) != null) {
        return world.getEntity(uuid);
      }
    }
    return null;
  }

  public List<LivingEntity> getEntitiesNearby(@Nonnull Entity target, @Nonnull ServerLevel world, double range) {
    List<LivingEntity> list = new ArrayList<>();

    for (Entity e : world.getAllEntities()) {
      if (e.getUUID() == target.getUUID())
        continue;
      if (e instanceof LivingEntity entity) {

        if (entity.distanceTo(entity) <= range) {
          list.add(entity);
        }
      }
    }

    return list;
  }

  public List<Player> getPlayersNearby(@Nonnull Entity target, @Nonnull ServerLevel world, double range) {
    List<Player> list = new ArrayList<>();

    for (Player entity : world.getPlayers(predi -> {
      return !(predi.getUUID() == target.getUUID());
    })) {
      if (entity.distanceTo(entity) <= range) {
        list.add(entity);
      }

    }

    return list;
  }

  @Nullable
  public ServerLevel getWorld(String id) {
    if (server.getWorld(id) != null) {
      return ((CraftWorld) server.getWorld(id)).getHandle();
    }
    return null;
  }

  public Entity getTargetEntity(LivingEntity target, int range) {
    EntityHitResult result = target.getTargetEntity(range);
    if (result != null) {
      return result.getEntity();
    }
    return null;
  }

}
