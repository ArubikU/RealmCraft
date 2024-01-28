package dev.arubik.realmcraft.MythicLib.Passive;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.arubik.realmcraft.MythicLib.SkillTag;

@SkillTag
public class WindMaestery extends SkillHandler<SimpleSkillResult> implements Listener {

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    private int cooldownTime = 30 * 20; // 30 seconds in ticks
    private Map<UUID, Long> cooldownMap = new HashMap<>();
    private Map<UUID, Integer> jumpCountMap = new HashMap<>();

    public WindMaestery() {
        super(false);
        registerModifiers("cooldown"); // Register the cooldown modifier
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        MMOPlayerData playerData = MMOPlayerData.get(player);
        if (!MMOPlayerData.has(player))
            return;
        if (playerData.getPassiveSkillMap().getSkill(this) != null) {
            Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
            cooldownTime = (int) (meta.getModifier("cooldown") * 20);
            if (canUseWindMastery(player)) {
                jumpCountMap.put(player.getUniqueId(), 2);
                player.setVelocity(player.getLocation().getDirection().multiply(1.5));
                startCooldown(player);
            }
        }
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata meta) {
    }

    private boolean canUseWindMastery(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldownMap.containsKey(playerId)) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        long lastCastTime = cooldownMap.get(playerId);
        return currentTime - lastCastTime >= cooldownTime;
    }

    private void startCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        cooldownMap.put(playerId, currentTime);
    }
}
