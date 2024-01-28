package dev.arubik.realmcraft.MythicLib.Passive;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.RealPlayer;
import dev.arubik.realmcraft.Api.Utils;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import dev.arubik.realmcraft.MythicLib.SkillTag;

@SkillTag
public class AncientBow extends SkillHandler<AttackSkillResult> implements Listener {

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    public AncientBow() {
        super(false);
        registerModifiers("damage-multiplier", "thunders"); // Register the soul-chance modifier
    }

    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player player) {

                MMOPlayerData playerData = MMOPlayerData.get(player);
                if (!MMOPlayerData.has(player))
                    return;
                if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                    if (RealPlayer.of(player).consumeItem(Material.GHAST_TEAR, 1)) {

                        Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                        double damageMultiplier = meta.getModifier("damage-multiplier");
                        double thunders = meta.getModifier("thunders");
                        for (int i = 0; i < thunders; i++) {
                            // play sound of amethyst crystal breaking
                            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
                            // strike lightning
                            player.getWorld().strikeLightning(event.getEntity().getLocation());
                        }
                        event.setDamage(event.getDamage() * damageMultiplier);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata arg0) {

        throw new UnsupportedOperationException("Unimplemented method 'getResult'");
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata arg1) {

        throw new UnsupportedOperationException("Unimplemented method 'whenCast'");
    }

    // ... (previously defined code)
}
