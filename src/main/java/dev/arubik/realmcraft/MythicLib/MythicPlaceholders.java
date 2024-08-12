package dev.arubik.realmcraft.MythicLib;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.LineConfig;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Targeter;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Managers.BloodMoon;
import dev.arubik.realmcraft.Managers.Depend;
import dev.arubik.realmcraft.MythicLib.Passive.ComboAttack;
import dev.arubik.realmcraft.MythicLib.Passive.StackedAttack;
import dev.arubik.realmcraft.Storage.StorageLore;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.MythicEntityType;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.inventory.InventoryItem;
import net.Indyuce.inventory.inventory.InventoryLookupMode;
import net.Indyuce.inventory.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.api.player.PlayerData;

public class MythicPlaceholders extends PlaceholderExpansion implements Depend {

    @Override
    public @NotNull String getAuthor() {
        return "Arubik";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "realmcraft-mythic";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return Depend.isPluginEnabled(this);
    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "PlaceholderAPI", "MythicMobs", "MythicLib" };
    }

    private String reParse(String parse) {
        if (realmcraft.getInteractiveConfig().getString("config.return_able_0", "false").equalsIgnoreCase("true")) {
            if (parse.contains("UNDEFINED")) {
                return "0";
            } else {
                return "1";
            }
        } else if (realmcraft.getInteractiveConfig().getString("config.return_able_boolean", "false")
                .equalsIgnoreCase("true")) {
            if (parse.contains("UNDEFINED")) {
                return "False";
            } else {
                return "True";
            }
        } else {
            if (parse.contains("UNDEFINED")) {
                return "UNDEFINED";
            } else {
                return "DEFINED";
            }
        }
    }

    private String reEmpty(String parse) {
        if (parse.contains("UNDEFINED")) {
            return "";
        } else {
            return parse;
        }
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "Player is null!";
        }
        LineConfig config = new LineConfig(identifier);

        if (config.getKey().equalsIgnoreCase("target")) {
            Targeter.preSetup(player);
            String localeidentifier = config.getString(new String[] { "localeidentifier" }, "name");

            Boolean returnDefine = false;
            Boolean returnEmpty = false;
            String extraPart = "";
            if (localeidentifier.toLowerCase().startsWith("define_")) {
                localeidentifier = localeidentifier.replace("define_", "");
                returnDefine = true;
            }
            if (localeidentifier.toLowerCase().startsWith("empty_")) {
                localeidentifier = localeidentifier.replace("empty_", "");
                returnEmpty = true;
            }
            if (localeidentifier.toLowerCase().startsWith("extra_")) {
                localeidentifier = localeidentifier.replace("extra_", "");
                // pc.log(Arrays.deepToString(localeidentifier.split("!")));
                extraPart = localeidentifier.split("!")[1].replace("<exclam>", "!");
                localeidentifier = localeidentifier.split("!")[0];
            }

            String targetResult = "UNDEFINED";
            if (Targeter.getCustomBlock(player) != null) {
                if (localeidentifier.equalsIgnoreCase("identifier")) {
                    targetResult = "CUSTOMBLOCK";
                }
                if (localeidentifier.equalsIgnoreCase("data")) {
                    targetResult = Targeter.getCustomBlockParsed(player);
                }
                if (returnDefine) {
                    return reParse(targetResult + extraPart);
                }
                if (returnEmpty) {
                    return reEmpty(targetResult + extraPart);
                }
                return targetResult + extraPart;
            }
            String a = Targeter.getBlock(player);
            if (a != null) {
                if (localeidentifier.equalsIgnoreCase("identifier")) {
                    targetResult = "VANILLABLOCK";
                }
                if (localeidentifier.equalsIgnoreCase("data")) {
                    targetResult = a;
                }
                if (returnDefine) {
                    return reParse(targetResult + extraPart);
                }
                if (returnEmpty) {
                    return reEmpty(targetResult + extraPart);
                }
                return targetResult + extraPart;
            }

            Entity target = Targeter.getTargetEntity(player);
            if ((target != null) && (target instanceof LivingEntity || target instanceof Player)) {
                LivingEntity LivingTarget = (LivingEntity) target;

                if (localeidentifier.equalsIgnoreCase("name")) {
                    if (target instanceof Player) {
                        targetResult = ((Player) target).getName();
                    } else {
                        targetResult = target.getCustomName();
                        if (MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget) != null) {
                            targetResult = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget)
                                    .getDisplayName();
                            if (targetResult == null) {
                                targetResult = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget)
                                        .getType().getInternalName();
                            }
                        } else {
                            targetResult = target.getName();
                            if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
                                if (MythicBukkit.inst().getAPIHelper().isMythicMob(target)) {
                                    targetResult = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(target)
                                            .getDisplayName();
                                }
                            }
                        }
                    }
                }
                if (localeidentifier.equalsIgnoreCase("identifier")) {
                    if (target instanceof Player) {
                        targetResult = "PLAYER";
                    } else {
                        targetResult = target.getType().toString();

                        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
                            if (MythicBukkit.inst().getAPIHelper().isMythicMob(target)) {
                                targetResult = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(target).getType()
                                        .getInternalName();
                            }
                        }

                    }
                }
                if (localeidentifier.equalsIgnoreCase("data")) {
                    if (target instanceof Player) {
                        targetResult = "PLAYER[" + ((Player) target).getName() + "]";
                    } else {
                        targetResult = target.getType().toString() + " Nametag[" + target.getCustomName() + "]";

                        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
                            if (MythicBukkit.inst().getAPIHelper().isMythicMob(target)) {
                                ActiveMob mob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(target);
                                targetResult = mob.getType()
                                        .getInternalName() + " Lvl[" + Utils.round(mob.getLevel()) + "] Nametag[" +
                                        ChatColor.stripColor(mob.getDisplayName()) + "] InternalType[CustomMob]";
                            }
                        }
                    }
                }
                if (localeidentifier.equalsIgnoreCase("mobtype")) {
                    if (target instanceof Player) {
                        targetResult = "PLAYER";
                    } else {
                        targetResult = target.getType().toString();
                        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
                            if (MythicBukkit.inst().getAPIHelper().isMythicMob(target)) {
                                targetResult = "MYTHICMOB";
                            }
                        } else {
                            targetResult = "VANILLA";
                        }
                    }
                }
                if (localeidentifier.equalsIgnoreCase("health")) {
                    targetResult = LivingTarget.getHealth() + "";
                }
                if (localeidentifier.equalsIgnoreCase("max_health")) {
                    if (MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget) != null) {
                        targetResult = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget).getEntity()
                                .getMaxHealth() + "";
                    } else {
                        targetResult = LivingTarget.getMaxHealth() + "";
                    }
                }

                if (localeidentifier.equalsIgnoreCase("equipment")) {

                }

                if (localeidentifier.equalsIgnoreCase("armor")) {
                    if (target instanceof Player) {
                        MMOPlayerData playerData = MMOPlayerData.get((Player) target);
                        StatMap statMap = playerData.getStatMap();
                        if (statMap.getStat("DEFENSE") > 0) {
                            targetResult = statMap.getStat("DEFENSE") + "";
                        } else {
                            targetResult = LivingTarget.getAttribute(Attribute.GENERIC_ARMOR).getValue() + "";
                        }
                    } else {
                        if (MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget) != null) {
                            targetResult = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget)
                                    .getArmor() + "";
                        } else {
                            targetResult = LivingTarget.getAttribute(Attribute.GENERIC_ARMOR).getValue() + "";
                        }
                    }
                }
                if (localeidentifier.equalsIgnoreCase("damage")) {
                    if (target instanceof Player) {
                        MMOPlayerData playerData = MMOPlayerData.get((Player) target);
                        StatMap statMap = playerData.getStatMap();
                        if (statMap.getStat("ATTACK_DAMAGE") > 0) {
                            targetResult = statMap.getStat("ATTACK_DAMAGE") + "";
                        } else {
                            targetResult = LivingTarget.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() + "";
                        }
                    } else {
                        if (MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget) != null) {
                            targetResult = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget)
                                    .getDamage() + "";
                        } else {
                            targetResult = LivingTarget.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() + "";
                        }
                    }
                }
                if (localeidentifier.equalsIgnoreCase("level")) {
                    if (target instanceof Player) {
                        if (Bukkit.getPluginManager().getPlugin("MMOCore") != null) {
                            targetResult = PlayerData.get(player).getLevel() + "";
                        }
                    } else {
                        if (MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget) != null) {
                            targetResult = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget)
                                    .getLevel() + "";
                        }
                    }

                }
                if (localeidentifier.equalsIgnoreCase("exp_level")) {
                    if (target instanceof Player) {
                        targetResult = ((Player) target).getExpToLevel() + "";
                    } else {
                        if (MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget) != null) {
                            targetResult = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(LivingTarget)
                                    .getPlayerKills() + "";
                        }
                    }

                }
                if (localeidentifier.equalsIgnoreCase("class")) {
                    if (target instanceof Player) {
                        targetResult = PlayerData.get((Player) player).getProfess().getName();
                    }
                }

                if (localeidentifier.split(",").length > 0) {
                    String[] args = localeidentifier.split(",");
                    if (args[0].equalsIgnoreCase("placeholder")) {
                        if (target instanceof Player) {
                            targetResult = PlaceholderAPI.setPlaceholders((Player) target, "%" + args[1] + "%");
                        }
                    }
                    if (args[0].equalsIgnoreCase("stat")) {
                        if (target instanceof Player) {
                            MMOPlayerData playerData = MMOPlayerData.get((Player) target);
                            StatMap statMap = playerData.getStatMap();
                            if (statMap.getInstance(args[1]) != null) {
                                targetResult = statMap.getInstance(args[1]).getTotal() + "";
                            }
                        }
                    }
                }

                if (realmcraft.getInteractiveConfig().getString("config.decimal_able", "true")
                        .equalsIgnoreCase("false")) {
                    if (targetResult.contains(".")) {
                        return targetResult.replace(".", ":").split(":")[0];
                    }
                } else {
                    return targetResult;
                }
                if (returnDefine) {
                    return reParse(targetResult.replace(".",
                            realmcraft.getInteractiveConfig().getString("config.decimal_separator", ".")) + extraPart);
                }
                if (returnEmpty) {
                    return reEmpty(targetResult.replace(".",
                            realmcraft.getInteractiveConfig().getString("config.decimal_separator", ".")) + extraPart);
                }

                return targetResult.replace(".",
                        realmcraft.getInteractiveConfig().getString("config.decimal_separator", ".")) + extraPart;
            }
            if (returnDefine) {
                return reParse(targetResult + extraPart);
            }
            if (returnEmpty) {
                return reEmpty(targetResult + extraPart);
            }
            return reParse("UNDEFINED" + extraPart);

        }

        if (config.getKey().equalsIgnoreCase("backpack")) {
            EquipmentSlot slot = EquipmentSlot.valueOf(config.getString(new String[] { "slot" }, "HAND"));
            RealNBT item = RealNBT.fromItemStack(player.getInventory().getItem(slot));

            return Arrays.toString(StorageLore.getItemsOfBackpack(item).toArray());

        }

        if (config.getKey().equalsIgnoreCase("rpginv")) {
            int slot = config.getInteger(new String[] { "slot" }, 0);
            PlayerDataManager dataManager = MMOInventory.plugin.getDataManager();
            for (InventoryItem item : dataManager.get(player).getItems(InventoryLookupMode.IGNORE_RESTRICTIONS)) {
                if (item.getSlot().getIndex() == slot) {
                    RealNBT itemNBT = RealNBT.fromItemStack(item.getItemStack());
                    return itemNBT.prettyPrint();
                }
            }
            return "[Empty]";

        }

        if (config.getKey().equalsIgnoreCase("bloodmoon")) {
            return BloodMoon.enabled.toString();
        }
        if (config.getKey().equalsIgnoreCase("StackedAttack")) {
            return StackedAttack.getAttackStacksMap().getOrDefault(player.getUniqueId(), 0).toString();
        }
        if (config.getKey().equalsIgnoreCase("ComboAttack")) {
            return ComboAttack.getComboMap().getOrDefault(player.getUniqueId(), 0).toString();
        }
        if (config.getKey().equalsIgnoreCase("MythicMobStats")) {
            String mob = config.getString(new String[] { "mobType", "mobtype", "mob" }, "ZOMBIE");
            Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mob);
            if (mythicMob.isPresent()) {
                MythicConfig mythicConfig = mythicMob.get().getConfig();
                double multiplier = config.getDouble(new String[] { "multiplier", "multi" }, 1);
                String stat = config.getString(new String[] { "stat", "statistic", "statisticType", "statistictype",
                        "statType", "stattype" }, "health");
                boolean levelModifier = config.getBoolean(
                        new String[] { "levelModifier", "levelmodifier", "levelMod", "levelmod" }, false);
                if (levelModifier) {
                    double value = mythicConfig.getDouble("LevelModifiers." + stat);
                    return String.valueOf(value * multiplier);
                } else {
                    double value = mythicConfig.getDouble(stat);
                    return String.valueOf(value * multiplier);
                }
            }
        }
        if (config.getKey().equalsIgnoreCase("MythicLoreMob")) {
            String mob = config.getString(new String[] { "mobType", "mobtype", "mob" }, "ZOMBIE");
            Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mob);
            if (mythicMob.isPresent()) {
                MythicConfig mythicConfig = mythicMob.get().getConfig();
                List<String> drops = mythicConfig.getStringList("Drops");
                int DropSlot = config.getInteger(new String[] { "dropSlot", "dropslot", "slot" }, 0);
                String RangeSeparator = config.getString(new String[] { "rangeSeparator", "rangeseparator", "rangeSep",
                        "rangesep", "sep" }, "-");
                String valueToGet = config.getString(new String[] { "valueToGet", "valuetoget", "value", "val" },
                        "drop");
                /*
                 * valueToGet: drop, chance, range
                 */
                if (DropSlot < drops.size()) {
                    String drop = drops.get(DropSlot);
                    if (!(drop.contains("{") && drop.contains("}"))) {
                        String[] Args = drop.split(" ");
                        if (Args.length == 0) {
                            return realmcraft.getInteractiveConfig().getString("translation.drop." + Args[0], Args[0]);
                        }
                        if (Args.length == 1) {
                            if (valueToGet.equalsIgnoreCase("drop")) {
                                return realmcraft.getInteractiveConfig().getString("translation.drop." + Args[0],
                                        Args[0]);
                            } else if (valueToGet.equalsIgnoreCase("range")) {
                                String range = Args[1];
                                range = range.replace("-", RangeSeparator);
                                range = range.replace("to", RangeSeparator);
                            }
                        }
                        if (Args.length == 2) {
                            if (valueToGet.equalsIgnoreCase("drop")) {
                                return realmcraft.getInteractiveConfig().getString("translation.drop." + Args[0],
                                        Args[0]);
                            } else if (valueToGet.equalsIgnoreCase("chance")) {
                                Double chance = Double.parseDouble(Args[1]);
                                chance = chance * 100;
                                return String.valueOf(chance);
                            } else if (valueToGet.equalsIgnoreCase("range")) {
                                String range = Args[1];
                                range = range.replace("-", RangeSeparator);
                                range = range.replace("to", RangeSeparator);
                            }
                        }

                    }
                }
            }
        }
        if (config.getKey().equalsIgnoreCase("MythicFactionMob")) {
            String mob = config.getString(new String[] { "mobType", "mobtype", "mob" }, "ZOMBIE");
            Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mob);
            if (mythicMob.isPresent()) {
                MythicConfig mythicConfig = mythicMob.get().getConfig();
                if (!mythicConfig.getFileConfiguration().contains(mob + ".Faction")) {
                    MythicEntityType mythicEntityType = mythicMob.get().getEntityType();
                    MythicEntityGroup mythicEntityGroup = MythicEntityGroup.getGroup(mythicEntityType);
                    return realmcraft.getInteractiveConfig().getString(
                            "translation.faction." + mythicEntityGroup.toString(),
                            mythicEntityGroup.toString());
                }
                String faction = mythicConfig.getString("Faction");
                return realmcraft.getInteractiveConfig().getString("translation.faction." + faction, faction);
            }
        }
        return "PlaceholderAPI is working!";
    }

    public enum MythicEntityGroup {
        UNDEAD(MythicEntityType.ZOMBIE, MythicEntityType.ZOMBIE_VILLAGER, MythicEntityType.HUSK,
                MythicEntityType.DROWNED, MythicEntityType.ZOMBIFIED_PIGLIN, MythicEntityType.ZOMBIFIED_PIGLIN_VILLAGER,
                MythicEntityType.ZOMBIE_HORSE,
                MythicEntityType.BABY_PIG_ZOMBIE, MythicEntityType.BABY_ZOMBIE,
                MythicEntityType.BABY_ZOMBIE_VILLAGER, MythicEntityType.SKELETON, MythicEntityType.WITHER_SKELETON,
                MythicEntityType.STRAY, MythicEntityType.SKELETON_HORSE),
        PIGLIN(MythicEntityType.PIGLIN, MythicEntityType.PIGLIN_BRUTE, MythicEntityType.BABY_PIGLIN),
        WITHER(MythicEntityType.WITHER, MythicEntityType.WITHER_SKELETON),
        END(MythicEntityType.ENDERMAN, MythicEntityType.ENDER_DRAGON, MythicEntityType.SHULKER),
        GUARDIAN(MythicEntityType.GUARDIAN, MythicEntityType.ELDER_GUARDIAN),
        PILLAGER(MythicEntityType.PILLAGER, MythicEntityType.VINDICATOR, MythicEntityType.EVOKER,
                MythicEntityType.ILLUSIONER);

        private final Set<MythicEntityType> types;

        MythicEntityGroup(MythicEntityType... types) {
            this.types = Set.of(types);
        }

        public Set<MythicEntityType> getTypes() {
            return types;
        }

        public static MythicEntityGroup getGroup(MythicEntityType type) {
            for (MythicEntityGroup group : MythicEntityGroup.values()) {
                if (group.getTypes().contains(type)) {
                    return group;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            // make first letter uppercase
            return name().substring(0, 1) + name().substring(1).toLowerCase();
        }
    }
}
