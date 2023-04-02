package dev.arubik.realmcraft.MythicLib;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.LineConfig;
import dev.arubik.realmcraft.Managers.Depend;
import dev.arubik.realmcraft.MythicLib.Passive.ComboAttack;
import dev.arubik.realmcraft.MythicLib.Passive.StackedAttack;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.MythicEntityType;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.lib.http.util.Args;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

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

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "Player is null!";
        }
        LineConfig config = new LineConfig(identifier);
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
                MythicEntityType.DROWNED, MythicEntityType.PIG_ZOMBIE, MythicEntityType.ZOMBIE_HORSE,
                MythicEntityType.PIG_ZOMBIE_VILLAGER, MythicEntityType.BABY_PIG_ZOMBIE, MythicEntityType.BABY_ZOMBIE,
                MythicEntityType.BABY_ZOMBIE_VILLAGER, MythicEntityType.SKELETON, MythicEntityType.WITHER_SKELETON,
                MythicEntityType.STRAY, MythicEntityType.SKELETON_HORSE),
        PIGLIN(MythicEntityType.PIGLIN, MythicEntityType.PIGLIN_BRUTE, MythicEntityType.PIG_ZOMBIE_VILLAGER,
                MythicEntityType.PIG_ZOMBIE, MythicEntityType.BABY_PIG_ZOMBIE, MythicEntityType.BABY_PIGLIN),
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
