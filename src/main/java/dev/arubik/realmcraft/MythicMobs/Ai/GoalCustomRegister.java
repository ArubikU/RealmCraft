package dev.arubik.realmcraft.MythicMobs.Ai;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.MobGoals;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.MythicLib.SkillTag;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.ai.Pathfinder;

public class GoalCustomRegister implements Listener {

    public static class AplicableGoal {
        Class<? extends Goal> clazz;
        String id;

        public AplicableGoal(String id, Class<? extends Goal> clazz) {
            this.clazz = clazz;
            this.id = id;
        }

        public <T extends Mob> boolean apply(T mob) {
            Goal<T> goal;
            try {
                goal = (Goal<T>) clazz.getDeclaredConstructors()[0].newInstance(mob);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | SecurityException e) {
                e.printStackTrace();
                return false;
            }
            Bukkit.getMobGoals().addGoal(mob, 0, goal);

            return true;
        }
    }

    private static Map<String, AplicableGoal> goals = new HashMap<>();

    public AplicableGoal getGoal(String id) {
        return goals.getOrDefault(id, null);
    }

    realmcraft instance;

    public GoalCustomRegister(realmcraft instance) {
        this.instance = instance;

        RealMessage.sendConsoleMessage("<green>Registering goals handlers...");
        Reflections reflections = new Reflections(".*");

        for (Class clazz : reflections.getTypesAnnotatedWith(AiTag.class)) {
            // get data from anotation "id() String"
            AiTag tag = (AiTag) clazz.getAnnotation(AiTag.class);
            String id = tag.id();
            AplicableGoal goal = new AplicableGoal(id, clazz);
            goals.put(id, goal);
            RealMessage.sendConsoleMessage("<green>Registered goal with id: <0>", id);
        }

    }

    public static <T extends Mob> GoalKey<T> getKey(String name, Class<? extends Goal> goalClass) {
        return (GoalKey<T>) GoalKey.of(Mob.class, NamespacedKey.minecraft(name));
    }
}
