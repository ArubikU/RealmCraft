
package dev.arubik.realmcraft.MythicLib.Cromes;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Api.Events.CustomEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.arubik.realmcraft.MythicLib.SkillTag;

@SkillTag
public class UntilTheMorning extends SkillHandler<AttackSkillResult> implements Listener {

    private static Map<UUID, Long> somnusCooldowns = new HashMap<>();
    private static Map<UUID, Double> lastDamageMap = new HashMap<>();

    public UntilTheMorning() {
        super(false);
        registerModifiers("skill_min", "skill_max", "min_follow_damage", "max_follow_damage", "somnus_delay",
                "somnus_cooldown");
    }

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    public NamespacedKey getKey() {
        return new NamespacedKey("realmcraft", getId().toLowerCase());
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata arg0) {
        return new AttackSkillResult(arg0);
    }

    @EventHandler
    public void onPlayerAttack(PlayerAttackEvent event) {
        Player player = event.getAttacker().getPlayer();
        if (!MMOPlayerData.has(player))
            return;

        MMOPlayerData playerData = MMOPlayerData.get(player);
        if (playerData.getPassiveSkillMap().getSkill(this) != null) {
            Skill metaSkill = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();

            double minSkillDamage = metaSkill.getModifier("skill_min");
            double maxSkillDamage = metaSkill.getModifier("skill_max");

            // Incremento de daño de habilidad y usuario
            double skillDamageIncrease = Utils.random(minSkillDamage, maxSkillDamage) / 100;
            double baseDamage = event.getDamage().getDamage();
            double finalDamage = baseDamage * (1 + skillDamageIncrease);
            event.getDamage().add(baseDamage * skillDamageIncrease, DamageType.MAGIC);
            lastDamageMap.put(player.getUniqueId(), finalDamage);
        }
    }

    @EventHandler
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity target) {
            if (!MMOPlayerData.has(player) || !lastDamageMap.containsKey(player.getUniqueId()))
                return;
            MMOPlayerData playerData = MMOPlayerData.get(player);

            Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
            if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                double minFollowDamage = meta.getModifier("min_follow_damage");
                double maxFollowDamage = meta.getModifier("max_follow_damage");
                double somnusDelay = meta.getModifier("somnus_delay");
                double somnusCooldown = meta.getModifier("somnus_cooldown");

                if (!isSomnusCooldownActive(player, somnusCooldown)) {
                    if (new CustomEvent(player, getLowerCaseId() + "_FIRED").callEvent()) {
                        setSomnusCooldown(player, somnusCooldown + somnusDelay);
                        Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
                            double followDamage = Utils.random(minFollowDamage, maxFollowDamage) / 100;

                            double finalFollowDamage = lastDamageMap.get(player.getUniqueId()) * followDamage;
                            target.damage(finalFollowDamage, player);

                        }, (long) (somnusDelay * 20));
                    }
                }
            }
        }
    }

    private void setSomnusCooldown(Player player, double somnusCooldown) {
        somnusCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (long) (somnusCooldown * 1000));
    }

    private boolean isSomnusCooldownActive(Player player, double somnusCooldown) {
        if (somnusCooldowns.containsKey(player.getUniqueId())) {
            long lastTime = somnusCooldowns.get(player.getUniqueId());
            long currentTime = System.currentTimeMillis();
            long cooldownMillis = (long) (somnusCooldown * 1000);

            return currentTime - lastTime < cooldownMillis;
        }
        return false;
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata arg1) {
    }
}