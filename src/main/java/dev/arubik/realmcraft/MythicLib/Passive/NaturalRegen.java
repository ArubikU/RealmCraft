package dev.arubik.realmcraft.MythicLib.Passive;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.RealPlayer;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.arubik.realmcraft.MythicLib.SkillTag;

@SkillTag
public class NaturalRegen extends SkillHandler<AttackSkillResult> implements Listener {

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    private double regenPercentage = 0.05;
    private int cooldownTime = 30; // 30 seconds in ticks
    private static Map<UUID, Long> cooldownMap = new HashMap<>();

    public NaturalRegen() {
        super(false);
        registerModifiers("percentage", "cooldown", "chance"); // Register the percentage and cooldown modifiers
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!MMOPlayerData.has(player))
                return;
            MMOPlayerData playerData = MMOPlayerData.get(player);
            if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                if (canTriggerSkill(player)) {

                    Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                    double probability = meta.getModifier("chance");

                    if (Utils.Chance(probability * 100, 100)) {
                        {

                            regenPercentage = meta.getModifier("percentage"); // Get the value from the modifier
                            cooldownTime = (int) (meta.getModifier("cooldown")); // Get the value from the modifier
                                                                                 // and
                                                                                 // convert to
                                                                                 // ticks
                            if (player != null) {
                                double maxHealth = player
                                        .getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)
                                        .getValue();
                                double currentHealth = player.getHealth();
                                double newHealth = maxHealth * regenPercentage;

                                if (currentHealth + newHealth > maxHealth) {

                                    double addedHealth = newHealth - ((currentHealth + newHealth) - maxHealth);

                                    player.setHealth(maxHealth);
                                    RealMessage.sendActionBar(player, "<red>Te has recuperado <green>"
                                            + Utils.round(addedHealth, 2) + "<red> de vida.");
                                    // play sound of regen
                                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.2f,
                                            1.0f);
                                    // play particles of healing potion in player feet
                                    player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation(),
                                            30);

                                } else {

                                    player.setHealth(currentHealth + newHealth);
                                    RealMessage.sendActionBar(player, "<red>Te has recuperado <green>"
                                            + Utils.round(newHealth, 2) + "<red> de vida.");
                                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.2f,
                                            1.0f);
                                    // play particles of healing potion in player feet
                                    player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation(),
                                            30);

                                }
                            }
                        }

                        startCooldown(player);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata meta) {
    }

    private boolean canTriggerSkill(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldownMap.containsKey(playerId)) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        long lastCastTime = cooldownMap.get(playerId);
        return currentTime - lastCastTime >= ((cooldownTime) * 1000);
    }

    private void startCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        cooldownMap.put(playerId, currentTime);
    }
}
