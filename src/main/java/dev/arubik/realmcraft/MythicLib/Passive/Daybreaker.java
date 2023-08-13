package dev.arubik.realmcraft.MythicLib.Passive;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.Utils;
import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Daybreaker extends SkillHandler<AttackSkillResult> implements Listener {

    public Daybreaker() {
        super(false);
        registerModifiers("chance"); // Register the soul-chance modifier
    }

    private static Map<UUID, UUID> LastKill = new HashMap<UUID, UUID>();

    @EventHandler
    public void onEntityDeath(PlayerKillEntityEvent event) {
        Player player = event.getPlayer();
        LivingEntity entity = event.getTarget();
        if (LastKill.containsKey(player.getUniqueId())) {
            if (LastKill.get(player.getUniqueId()) == event.getTarget().getUniqueId()) {
                return;
            } else {
                LastKill.put(player.getUniqueId(), event.getTarget().getUniqueId());
            }
        } else {
            LastKill.put(player.getUniqueId(), event.getTarget().getUniqueId());
        }

        MMOPlayerData playerData = MMOPlayerData.get(player);
        if (playerData.getPassiveSkillMap().getSkill(this) != null) {
            Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
            double soulChance = meta.getModifier("chance");
            if (Utils.Chance(soulChance * 100, 100)) {
                // play sound of amethyst crystal breaking
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1, 1);
                // drop gold ingot or gold nugget
                if (Utils.Chance(1, 2)) {
                    entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.GOLD_INGOT));
                } else {
                    ItemStack nugget = new ItemStack(Material.GOLD_NUGGET);
                    nugget.setAmount(Utils.randomNumer(2, 8));
                    entity.getWorld().dropItemNaturally(entity.getLocation(), nugget);
                }
            }

            // play gold break particle and sound
            entity.getWorld().spawnParticle(Particle.BLOCK_CRACK, entity.getLocation(), 10,
                    Material.GOLD_BLOCK.createBlockData());
        }

    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getResult'");
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'whenCast'");
    }

    // ... (previously defined code)
}
