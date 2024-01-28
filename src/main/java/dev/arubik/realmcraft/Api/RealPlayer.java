package dev.arubik.realmcraft.Api;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.party.PartyModuleType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class RealPlayer {
    Player player;

    public RealPlayer(Player player) {
        this.player = player;
    }

    public boolean isTeam(Player otherPlayer) {
        net.Indyuce.mmocore.api.player.PlayerData playerData = net.Indyuce.mmocore.api.player.PlayerData
                .get(player);
        // verify if player is from the same Clan or Party
        if (MMOCore.plugin.partyModule.getParty(playerData).hasMember(otherPlayer))
            return true;
        if (MMOCore.plugin.guildModule.getGuild(playerData).hasMember(otherPlayer))
            return true;
        return false;
    }

    public static RealPlayer of(Player player) {
        return new RealPlayer(player);
    }

    public boolean consumeItem(Material mat, int reamount) {
        if (player.getInventory().contains(mat, reamount)) {
            player.getInventory().iterator().forEachRemaining(item -> {
                int amount = reamount;
                if (item != null && item.getType() == mat) {
                    if (item.getAmount() > amount) {
                        item.setAmount(item.getAmount() - amount);
                        return;
                    } else {
                        amount -= item.getAmount();
                        item.setAmount(0);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public StatMap getStatMap() {
        return MMOPlayerData.get(player).getStatMap();
    }

    public SkillMetadata getSkillMetadata(Skill skill) {
        MMOPlayerData playerData = MMOPlayerData.get(player);
        SkillMetadata skilld = new SkillMetadata(skill, playerData);
        return skilld;
    }

    public Player getPlayer() {
        return player;
    }

    public void DamageEntity(double d, String e, DamageType dt, LivingEntity... entities) {

        DamageMetadata damage = new DamageMetadata();
        damage.add(d, Element.valueOf(e), dt);
        AttackMetadata attack = new AttackMetadata(damage, getStatMap());
        for (LivingEntity target : entities) {
            MythicLib.plugin.getDamage().damage(attack, target, false);
        }

    }

    public BlockFace getBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
        if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding())
            return null;
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }

    public Block getTargetBlock(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
        if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding())
            return null;
        Block targetBlock = lastTwoTargetBlocks.get(1);
        return targetBlock;
    }

    private static Axis convertBlockFaceToAxis(BlockFace face) {
        switch (face) {
            case NORTH:
            case SOUTH:
                return Axis.Z;
            case EAST:
            case WEST:
                return Axis.X;
            case UP:
            case DOWN:
                return Axis.Y;
            default:
                return Axis.X;
        }
    }

    private static BlockFace invertBlockFace(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.NORTH;
            case EAST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.EAST;
            case UP:
                return BlockFace.DOWN;
            case DOWN:
                return BlockFace.UP;
            default:
                return BlockFace.NORTH;
        }
    }

    public enum PlaceType {
        BLOCK,
        ENTITY;

        @Nullable
        public static PlaceType getPlaceType(@NotNull ItemStack material) {
            if (material.getType().isBlock()) {
                return BLOCK;
            }
            if (material.getItemMeta() instanceof SpawnEggMeta) {
                return ENTITY;
            }
            return null;
        }
    }

    public boolean PlaceBlock(int range) {

        if (range <= 3.5 && player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
            return false;
        }
        if (range <= 5.5 && player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return false;
        }

        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR)
            return false;

        // get item in hand
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR)
            return false;
        // check if item is placeable
        PlaceType type = PlaceType.getPlaceType(itemInHand);
        if (type == null)
            return false;
        // get block to place
        RealMessage.sendConsoleMessage("Placing block");

        Block targetBlock = getTargetBlock(player);
        BlockFace blockFace = getBlockFace(player);
        if (targetBlock == null || blockFace == null)
            return false;
        // calc distance
        Location playerLocation = player.getLocation();
        Location targetBlockLocation = targetBlock.getLocation();
        double distance = playerLocation.distance(targetBlockLocation);
        if (distance > range)
            return false;
        // check if block is
        // if (!targetBlock.getType().isOccluding())
        // return false;
        targetBlock = targetBlock.getRelative(blockFace);
        if (!(targetBlock.getType() == Material.AIR))
            return false;
        ItemStack HAND_ITEM = itemInHand.clone();
        if (type.equals(PlaceType.BLOCK)) {
            BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(targetBlock,
                    targetBlock.getState(), targetBlock,
                    HAND_ITEM, player, true);

            MythicLib.plugin.getServer().getPluginManager().callEvent(blockPlaceEvent);

            if (blockPlaceEvent.isCancelled())
                return false;

            // verify if targetBlock collides with player
            if (targetBlock.getBoundingBox().overlaps(player.getBoundingBox())) {
                return false;
            }

            // place block
            targetBlock.setType(HAND_ITEM.getType());
            // verify if is a special block
            if (HAND_ITEM.getType() == Material.PLAYER_HEAD || HAND_ITEM.getType() == Material.PLAYER_WALL_HEAD) {
                SkullMeta skullMeta = (SkullMeta) HAND_ITEM.getItemMeta();
                Skull skull = (Skull) targetBlock.getState();
                skull.setOwningPlayer(skullMeta.getOwningPlayer());
                skull.setPlayerProfile(skullMeta.getPlayerProfile());
                skull.update();
            }

            RealNBT nbtItem = RealNBT.fromItemStack(itemInHand);
            Double blockIdDouble = nbtItem.getDouble("MMOITEMS_BLOCK_ID", -1.0);
            Integer blockId = blockIdDouble.intValue();
            if (!(blockId > 160 || blockId < 1 || blockId == 54)) {

                CustomBlock block = MMOItems.plugin.getCustomBlocks().getBlock(blockId); // stores the custom block
                targetBlock.setType(block.getState().getType(), false);
                targetBlock.setBlockData(block.getState().getBlockData(), false);

            }
            // verify if block have direction
            if (targetBlock.getBlockData() instanceof org.bukkit.block.data.Directional) {
                org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) targetBlock
                        .getBlockData();
                directional.setFacing(blockFace);
                targetBlock.setBlockData(directional);
            } else if (targetBlock.getBlockData() instanceof org.bukkit.block.data.Rotatable) {
                org.bukkit.block.data.Rotatable rotatable = (org.bukkit.block.data.Rotatable) targetBlock
                        .getBlockData();

                BlockFace blockFace2 = invertBlockFace(blockFace);
                if (blockFace2 == BlockFace.UP || blockFace2 == BlockFace.DOWN) {
                    blockFace2 = BlockFace.NORTH;
                } else {
                    switch (HAND_ITEM.getType()) {
                        case PLAYER_HEAD: {
                            targetBlock.setType(Material.PLAYER_WALL_HEAD);
                            SkullMeta skullMeta = (SkullMeta) HAND_ITEM.getItemMeta();
                            Skull skull = (Skull) targetBlock.getState();
                            skull.setOwningPlayer(skullMeta.getOwningPlayer());
                            skull.setPlayerProfile(skullMeta.getPlayerProfile());
                            skull.update();
                            break;
                        }
                        case WITHER_SKELETON_SKULL: {
                            targetBlock.setType(Material.WITHER_SKELETON_WALL_SKULL);
                            break;
                        }
                        case SKELETON_SKULL: {
                            targetBlock.setType(Material.SKELETON_WALL_SKULL);
                            break;
                        }
                        case ZOMBIE_HEAD: {
                            targetBlock.setType(Material.ZOMBIE_WALL_HEAD);
                            break;
                        }
                        case CREEPER_HEAD: {
                            targetBlock.setType(Material.CREEPER_WALL_HEAD);
                            break;
                        }
                        case DRAGON_HEAD: {
                            targetBlock.setType(Material.DRAGON_WALL_HEAD);
                            break;
                        }
                        default:
                            break;

                    }
                }

                rotatable.setRotation(blockFace2);
                targetBlock.setBlockData(rotatable);
            } else if (targetBlock.getBlockData() instanceof org.bukkit.block.data.Orientable) {
                org.bukkit.block.data.Orientable orientable = (org.bukkit.block.data.Orientable) targetBlock
                        .getBlockData();
                orientable.setAxis(convertBlockFaceToAxis(blockFace));
                targetBlock.setBlockData(orientable);
            } else {
                targetBlock.setBlockData(targetBlock.getBlockData());
            }

        } else if (type.equals(PlaceType.ENTITY)) {
            SpawnEggMeta meta = (SpawnEggMeta) itemInHand.getItemMeta();

        }
        // consume item
        if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {

            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }

        // play player animation
        player.swingMainHand();
        return true;
    }

}
