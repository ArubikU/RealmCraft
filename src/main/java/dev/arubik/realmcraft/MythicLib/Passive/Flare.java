package dev.arubik.realmcraft.MythicLib.Passive;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.RealPlayer;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;

public class Flare extends SkillHandler<AttackSkillResult> implements Listener {

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    public Flare() {
        super(false);
        registerModifiers("chance", "radius", "ticks", "damage");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            MMOPlayerData playerData = MMOPlayerData.get(player);
            if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                double flareProbability = meta.getModifier("chance");

                if (Utils.Chance(flareProbability * 100, 100)) {
                    double flareRadius = meta.getModifier("radius");
                    double doubleflareTicks = meta.getModifier("ticks");
                    double flareDamage = meta.getModifier("damage");
                    // convert ticks to int
                    int flareTicks = (int) doubleflareTicks;
                    if (player != null) {
                        triggerFlareEffect(player, flareTicks, flareRadius, flareDamage);
                    }

                }
            }

        }
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata arg0) {
        return new AttackSkillResult(arg0);
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata meta) {
    }

    private void triggerFlareEffect(Player player, int ticks, double flareRange, double flareDamage) {

        for (LivingEntity nearbyEntity : player.getWorld().getLivingEntities()) {
            if (nearbyEntity != player && nearbyEntity.getLocation().distance(player.getLocation()) <= flareRange) {
                RealPlayer.of(player).DamageEntity(flareDamage, "FIRE", DamageType.SKILL, nearbyEntity);
                nearbyEntity.setFireTicks(ticks); // Set the entity on fire for 3 seconds (60 ticks)
                player.getWorld().spawnParticle(Particle.FLAME, nearbyEntity.getLocation(), 30);
                player.getWorld().playSound(nearbyEntity.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
            }
        }
    }
}
