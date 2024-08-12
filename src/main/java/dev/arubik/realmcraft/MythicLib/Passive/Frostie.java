package dev.arubik.realmcraft.MythicLib.Passive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealPlayer;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;

import dev.arubik.realmcraft.MythicLib.SkillTag;

@SkillTag
public class Frostie extends SkillHandler<AttackSkillResult> implements Listener {

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    private int slowDuration = 60; // 3 seconds in ticks

    public Frostie() {
        super(false);
        registerModifiers("chance", "ticks", "level"); // Register the chance and ticks modifiers
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!MMOPlayerData.has(player))
                return;
            MMOPlayerData playerData = MMOPlayerData.get(player);
            if (event.getDamager() instanceof LivingEntity liv) {
                if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                    Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                    double probability = meta.getModifier("chance");

                    if (Utils.Chance(probability * 100, 100)) {

                        slowDuration = (int) meta.getModifier("ticks"); // Get the value from the modifier and cast to
                                                                        // int
                        int slowlevel = (int) meta.getModifier("level");

                        applySlowEffect((LivingEntity) liv, slowlevel);

                    }
                }
            }
        }
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata meta) {
    }

    private void applySlowEffect(LivingEntity entity, int slowlevel) {
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDuration, slowlevel));
        // generate particles
        entity.getWorld().spawnParticle(Particle.SNOW_SHOVEL, entity.getLocation(), 30);
        entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.BLOCK_SNOW_BREAK, 1.0f, 1.0f);

        if (entity instanceof Player targetplayer) {

            int secondsOfSlow = slowDuration / 20;
            // fadein = 20%
            // stay = 60%
            // fadeout = 20%
            long fadeIn = (long) (secondsOfSlow * 0.2);
            long stay = (long) (secondsOfSlow * 0.6);
            long fadeOut = (long) (secondsOfSlow * 0.2);

            RealMessage.sendTittle(targetplayer, "<#42bcf5><bold>\u2744 Estas congelado! \u2744", fadeIn, stay,
                    fadeOut);

            playersFrozen.put(targetplayer.getUniqueId(), slowlevel);
            // remove the effect after the duration
            Utils.Delay(() -> {
                if (playersFrozen.containsKey(targetplayer.getUniqueId())) {
                    playersFrozen.remove(targetplayer.getUniqueId());
                    // remvoe the slowness effect
                    targetplayer.removePotionEffect(PotionEffectType.SLOW);
                    RealMessage.sendTittle(targetplayer, "<#42bcf5><bold>\u2744 Estas descongelado! \u2744");
                }
            }, slowDuration);
        }
    }

    private static Map<UUID, Integer> playersFrozen = new HashMap<UUID, Integer>();

    // verify when consume a milk bucket
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            if (playersFrozen.containsKey(event.getPlayer().getUniqueId())) {
                RealMessage.sendActionBar(event.getPlayer(), "<red>Espera a que te descongeles!");
                event.setCancelled(true);
            }
        }
    }

    @Override
    public AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }
}
