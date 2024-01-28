package dev.arubik.realmcraft.MythicMobs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.MMOCoreAPI;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;

public class ModifyLevel implements ITargetedEntitySkill, Depend {

    protected final String type;
    protected final int radius;
    protected final int modifier;
    protected final int maxLevel;
    protected final double multiplier;
    public static HashMap<String, Integer> maxTimes = new HashMap<>();
    protected final String playerLevelModifier;

    public static enum ModifierLevelTypes {
        NEAR,
        FAR,
        GREATEST,
        LEAST,
        RANDOM,
        AVERAGE,
        MAXAVERAGE,
        MINAVERAGE;

        public static ModifierLevelTypes fromString(String text) {
            for (ModifierLevelTypes b : ModifierLevelTypes.values()) {
                if (b.name().equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return NEAR;
        }
    }

    public static enum PlayerLevelModifier {
        GREATEST,
        LEAST,
        RANDOM,
        MAIN,
        AVERAGE;

        public static PlayerLevelModifier fromString(String text) {
            for (PlayerLevelModifier b : PlayerLevelModifier.values()) {
                if (b.name().equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return MAIN;
        }

    }

    public class Setteable<T> {
        private T value;

        public Setteable(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    private final int maxTimesAmount;

    public ModifyLevel(MythicLineConfig config) {
        this.type = config.getString(new String[] { "type", "t" }, "NEAR");
        this.radius = config.getInteger(new String[] { "radius", "r" }, 0);
        this.modifier = config.getInteger(new String[] { "modifier", "m" }, 0);
        this.playerLevelModifier = config.getString(new String[] { "playerLevelModifier", "plm" }, "MAIN");
        this.maxTimesAmount = config.getInteger(new String[] { "maxTimes", "mt" }, 100);
        this.multiplier = config.getDouble(new String[] { "multiplier", "ml" }, 1);
        this.maxLevel = config.getInteger(new String[] { "maxLevel", "ml" }, 400);
        RealMessage.sendConsoleMessage(DebugType.MODIFYLEVEL,
                "Dumping Data: " + type + " " + radius + " " + modifier + " " + playerLevelModifier + "");
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        LivingEntity LivingMob = (LivingEntity) BukkitAdapter.adapt(target);

        if (maxTimes.containsKey(LivingMob.getUniqueId().toString()) == false) {
            maxTimes.put(LivingMob.getUniqueId().toString(), maxTimesAmount);
        }

        if (maxTimes.get(LivingMob.getUniqueId().toString()) > 0) {
            maxTimes.put(LivingMob.getUniqueId().toString(), maxTimes.get(LivingMob.getUniqueId().toString()) - 1);

            MythicBukkit.inst().getMobManager().isActiveMob(target);
            ActiveMob mob = MythicBukkit.inst().getMobManager().getMythicMobInstance(target);
            if (mob == null || target == null) {
                RealMessage.sendConsoleMessage(DebugType.MODIFYLEVEL, "Mob is null");
                return SkillResult.ERROR;
            }
            Map<Player, Double> players = new HashMap<>();
            Map<Player, Integer> levels = new HashMap<>();
            PlayerLevelModifier playerLevelModifier = PlayerLevelModifier.fromString(this.playerLevelModifier);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(LivingMob.getWorld())) {
                    Double distance = LivingMob.getLocation().distance(player.getLocation());
                    if (distance <= radius) {
                        players.put(player, distance);
                        levels.put(player, getPlayerLevel(player, playerLevelModifier));
                    }
                }
            }
            // verify if players is empty
            if (players.isEmpty()) {
                maxTimes.put(LivingMob.getUniqueId().toString(), maxTimes.get(LivingMob.getUniqueId().toString()) + 1);
                return SkillResult.SUCCESS;
            }
            RealMessage.sendConsoleMessage(DebugType.MODIFYLEVEL,
                    "Players: " + Arrays.toString(players.keySet().stream().map(plauer -> plauer.getName()).toArray()));
            Setteable<Double> newLevelOpt = new Setteable<Double>(0.0);
            switch (ModifierLevelTypes.fromString(type)) {
                case NEAR:
                    players.entrySet().stream().min(Map.Entry.comparingByValue()).ifPresent(player -> {
                        Double newlevel = levels.get(player.getKey()) * multiplier + modifier;
                        if (newlevel < 1)
                            newlevel = 1.0;
                        newLevelOpt.set(newlevel);
                    });
                    break;
                case FAR:
                    players.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(player -> {
                        Double newlevel = levels.get(player.getKey()) * multiplier + modifier;
                        if (newlevel < 1)
                            newlevel = 1.0;
                        newLevelOpt.set(newlevel);
                    });
                    break;
                case RANDOM:
                    players.entrySet().stream().findAny().ifPresent(player -> {
                        Double newlevel = levels.get(player.getKey()) * multiplier + modifier;
                        if (newlevel < 1)
                            newlevel = 1.0;
                        newLevelOpt.set(newlevel);
                    });
                    break;
                default: {
                    switch (ModifierLevelTypes.valueOf(type)) {
                        case GREATEST: {

                            Double newlevel = levels.values().stream().max(Integer::compare).get() * multiplier
                                    + modifier;
                            if (newlevel < 1)
                                newlevel = 1.0;
                            newLevelOpt.set(newlevel);
                            break;
                        }
                        case LEAST: {

                            Double newlevel = levels.values().stream().min(Integer::compare).get() * multiplier
                                    + modifier;
                            if (newlevel < 1)
                                newlevel = 1.0;
                            newLevelOpt.set(newlevel);
                            break;
                        }
                        case AVERAGE: {
                            Double newlevel = levels.values().stream().mapToDouble(Integer::doubleValue).average()
                                    .getAsDouble() * multiplier + modifier;
                            if (newlevel < 1)
                                newlevel = 1.0;
                            newLevelOpt.set(newlevel);
                            break;
                        }
                        case MAXAVERAGE: {
                            Double aver = levels.values().stream().mapToDouble(Integer::doubleValue).average()
                                    .getAsDouble();
                            Integer maxLevel = levels.values().stream().max(Integer::compare).get();
                            Double newlevel = ((aver + maxLevel) / 2) * multiplier + modifier;
                            if (newlevel < 1)
                                newlevel = 1.0;
                            newLevelOpt.set(newlevel);
                            break;
                        }
                        case MINAVERAGE: {
                            Double aver = levels.values().stream().mapToDouble(Integer::doubleValue).average()
                                    .getAsDouble();
                            Integer minLevel = levels.values().stream().min(Integer::compare).get();
                            Double newlevel = ((aver + minLevel) / 2) * multiplier + modifier;
                            if (newlevel < 1)
                                newlevel = 1.0;
                            newLevelOpt.set(newlevel);
                            break;
                        }

                        default:
                            break;
                    }

                    break;
                }
            }

            try {
                if (newLevelOpt.get().intValue() < 1) {
                    newLevelOpt.set(1.0);
                }
                mob.setLevel(newLevelOpt.get().intValue() > maxLevel ? maxLevel : newLevelOpt.get().intValue());
            } catch (Throwable ta) {
            }

            RealMessage.sendConsoleMessage(DebugType.MODIFYLEVEL, "Mob Level: " + mob.getLevel());
        }
        return SkillResult.SUCCESS;
    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MythicMobs", "MMOCore" };
    }

    public int getPlayerLevel(Player player) {
        PlayerData data = PlayerData.get(player);
        return data.getLevel();
    }

    public int getPlayerLevel(Player player, PlayerLevelModifier modifier) {
        PlayerData data = PlayerData.get(player);
        RealMessage.sendConsoleMessage(DebugType.MODIFYLEVEL,
                "Player: " + player.getName() + " Profess: " + data.getProfess().getName());
        RealMessage.sendConsoleMessage(DebugType.MODIFYLEVEL, "Level: " + data.getLevel());
        switch (modifier) {
            case GREATEST:
                return MMOCore.plugin.classManager.getAll().stream()
                        .mapToInt(classInfo -> {
                            if (data.getClassInfo(classInfo) == null) {
                                return 1;
                            } else {
                                return data.getClassInfo(classInfo).getLevel();
                            }
                        }).max().getAsInt();
            case LEAST:
                return MMOCore.plugin.classManager.getAll().stream()
                        .mapToInt(classInfo -> {
                            if (data.getClassInfo(classInfo) == null) {
                                return 1;
                            } else {
                                return data.getClassInfo(classInfo).getLevel();
                            }
                        }).min().getAsInt();
            case RANDOM:
                return MMOCore.plugin.classManager.getAll().stream()
                        .mapToInt(classInfo -> {
                            if (data.getClassInfo(classInfo) == null) {
                                return 1;
                            } else {
                                return data.getClassInfo(classInfo).getLevel();
                            }
                        }).findAny().getAsInt();
            case AVERAGE:
                return (int) MMOCore.plugin.classManager.getAll().stream()
                        .mapToInt(classInfo -> {
                            if (data.getClassInfo(classInfo) == null) {
                                return 1;
                            } else {
                                return data.getClassInfo(classInfo).getLevel();
                            }
                        }).average().getAsDouble();
            default:
                return data.getLevel();
        }
    }
}