package dev.arubik.realmcraft.MythicMobs;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;

public class isNotFromSpawner implements ICasterCondition {

    @Override
    public boolean check(SkillCaster arg0) {
        Entity le = BukkitAdapter.adapt(arg0.getEntity());
        if (!le.hasMetadata("fromSpawner"))
            return false;
        return le.getMetadata("fromSpawner").get(0).asBoolean();
    }

}
