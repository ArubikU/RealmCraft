
package dev.arubik.realmcraft.MythicLib.Cromes;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.Utils;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;

import dev.arubik.realmcraft.MythicLib.SkillTag;

@SkillTag
public class SecretVote extends SkillHandler<AttackSkillResult> implements Listener {

    public SecretVote() {
        super(false);
        registerModifiers("min_damage", "max_damage", "chance", "min_damage_mhp", "max_damage_mhp");
    }

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata arg0) {
        return new AttackSkillResult(arg0);
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata meta) {
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity target) {
            if (!MMOPlayerData.has(player))
                return;

            MMOPlayerData playerData = MMOPlayerData.get(player);

            if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                double minDamage = meta.getModifier("min_damage");
                double maxDamage = meta.getModifier("max_damage");

                // Incremento de daño base
                double damageIncrease = Utils.random(minDamage, maxDamage) / 100;
                event.setDamage(event.getDamage() * (1 + damageIncrease));

                // Probabilidad de daño adicional a objetivos con mayor porcentaje de vida
                double chance = meta.getModifier("chance");
                if (Utils.Chance(chance, 100)) {
                    double minDamageMHP = meta.getModifier("min_damage_mhp");
                    double maxDamageMHP = meta.getModifier("max_damage_mhp");

                    // Incremento de daño adicional a objetivos con mayor porcentaje de vida
                    double mhpDamageIncrease = Utils.random(minDamageMHP, maxDamageMHP) / 100;
                    double targetMHP = target.getHealth()
                            / target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();

                    if (targetMHP > player.getHealth()
                            / player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) {
                        event.setDamage(event.getDamage() * (1 + mhpDamageIncrease));
                    }
                }
            }
        }
    }
}