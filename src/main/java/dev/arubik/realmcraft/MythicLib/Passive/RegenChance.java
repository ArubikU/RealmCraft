package dev.arubik.realmcraft.MythicLib.Passive;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.MythicLib.SkillTag;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;

@SkillTag
public class RegenChance extends SkillHandler<AttackSkillResult> implements Listener {

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    public RegenChance() {
        super(false);
        registerModifiers("probability", "health-regen");
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {

    }

    public void cast(Player player, Skill skillMeta) {
        final double probability = skillMeta.getModifier("probability");
        final double damageMultiplier = skillMeta.getModifier("health-regen");
        if (Utils.Chance(probability, 100)) {
            double maxHealth = MMOPlayerData.get(player).getStatMap().getStat("MAX_HEALTH");
            double currentHealth = player.getHealth();
            double newHealth = (maxHealth / 100) * damageMultiplier;
            if (currentHealth + newHealth > maxHealth) {
                player.setHealth(maxHealth);
            } else {
                player.setHealth(currentHealth + newHealth);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {

            MMOPlayerData data = MMOPlayerData.get(player);
            PassiveSkill skill = data.getPassiveSkillMap().getSkill(this);
            if (skill == null)
                return;
            if (!MMOPlayerData.has(player))
                return;
            MMOPlayerData playerData = MMOPlayerData.get(player);

            Skill skillMeta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
            this.cast(player, skillMeta);
        }
    }

}
