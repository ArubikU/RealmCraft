package dev.arubik.realmcraft.MythicLib.Passive;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.RealPlayer;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import dev.arubik.realmcraft.MythicLib.SkillTag;

@SkillTag
public class NightHug extends SkillHandler<AttackSkillResult> implements Listener {

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    private double stealProbability = 0.05;
    private double stealHealth = 10;

    private static List<UUID> mobs = new ArrayList<>();

    public NightHug() {
        super(false);
        registerModifiers("chance", "min-steal-health", "max-steal-health", "darkness"); // Register the probability and
                                                                                         // stealHealth
        // modifiers
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (mobs.contains(event.getEntity().getUniqueId())) {
            mobs.remove(event.getEntity().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerAttackEvent event) {
        boolean continueEvent = false;
        for (DamageType dtype : event.getAttack().getDamage().collectTypes()) {
            if (dtype == DamageType.SKILL) {
                continueEvent = true;
                break;
            }
        }
        if (!continueEvent)
            return;
        Player player = event.getPlayer();
        MMOPlayerData playerData = MMOPlayerData.get(player);
        if (!MMOPlayerData.has(player))
            return;
        if (playerData.getPassiveSkillMap().getSkill(this) != null) {
            RealMessage.sendConsoleMessage("NightHug triggered");

            LivingEntity target = event.getEntity();
            if (mobs.contains(target.getUniqueId())) {
                return;
            }
            Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
            stealProbability = meta.getModifier("chance"); // Get the value from the modifier
            stealHealth = (new Random()
                    .nextDouble(meta.getModifier("max-steal-health") - meta.getModifier("min-steal-health"))
                    + meta.getModifier("min-steal-health"));

            stealHealth = stealHealth / 100;

            if (Utils.Chance(stealProbability * 100, 100)) {
                if (player != null && target != null) {
                    mobs.add(target.getUniqueId());
                    double currentHealth = player.getHealth();

                    double maxHealth = player
                            .getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();

                    double newHealth = stealHealth * target.getHealth();
                    newHealth = Double.parseDouble(Utils.round(newHealth, 2));
                    if (currentHealth + newHealth > maxHealth) {

                        double addedHealth = newHealth - ((currentHealth + newHealth) - maxHealth);

                        player.setHealth(maxHealth);
                        RealMessage.sendActionBar(player, "<#445878>Has robado <green>"
                                + Utils.round(addedHealth, 2) + "<#445878> de vida.");
                        // play sound of regen
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.2f,
                                1.0f);
                        // play particles of healing potion in player feet
                        player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation(),
                                30);

                    } else {

                        player.setHealth(currentHealth + newHealth);
                        RealMessage.sendActionBar(player, "<#445878>Has robado <green>"
                                + newHealth + "<#445878> de vida.");
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.2f,
                                1.0f);
                        // play particles of healing potion in player feet
                        player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation(),
                                30);

                    }

                    int darkness = (int) meta.getModifier("darkness");
                    if (target instanceof Player targetPlayer) {
                        RealMessage.sendTittle(targetPlayer, "<#445878>La noche parece acercarse", 1, 3, 1);
                        // add darkness potion effect
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, darkness));
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
}
