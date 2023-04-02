package dev.arubik.realmcraft.MythicLib.Passive;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.Utils;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import lombok.Getter;

public class StackedAttack extends SkillHandler<AttackSkillResult> implements Listener {

    public StackedAttack() {
        super(false);
        registerModifiers("probability", "damage-multiplier", "attack-stacks");
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Getter
    private static Map<UUID, Integer> attackStacksMap = new HashMap<>();

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = (LivingEntity) skillMeta.getTargetEntityOrNull();
        if (target == null)
            return;
        final double probability = skillMeta.getModifier("probability");
        final double damageMultiplier = skillMeta.getModifier("damage-multiplier");
        final int attackStacks = new Double(skillMeta.getModifier("attack-stacks")).intValue();
        final Player player = skillMeta.getCaster().getPlayer();
        if (attackStacksMap.containsKey(player.getUniqueId())) {
            int currentStacks = attackStacksMap.get(player.getUniqueId());
            if (!(currentStacks >= attackStacks)) {
                attackStacksMap.put(player.getUniqueId(), currentStacks + 1);
                return;
            }
        } else {
            attackStacksMap.put(player.getUniqueId(), 1);
            return;
        }
        if (Utils.Chance(probability, 100)) {
            skillMeta.getAttack().getDamage()
                    .multiplicativeModifier(1 + skillMeta.getModifier("damage-multiplier") / 100, DamageType.SKILL);

            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, target.getHeight() / 2, 0), 32,
                    0, 0, 0, .05);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1, 1.5f);

        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerAttackEvent event) {
        MMOPlayerData data = event.getAttacker().getData();
        LivingEntity target = event.getEntity();
        PassiveSkill skill = data.getPassiveSkillMap().getSkill(this);
        if (skill == null)
            return;

        skill.getTriggeredSkill().cast(new TriggerMetadata(event.getAttacker(), event.getEntity()));
    }

}
