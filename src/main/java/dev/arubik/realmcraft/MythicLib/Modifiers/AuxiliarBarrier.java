package dev.arubik.realmcraft.MythicLib.Modifiers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealPlayer;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.MythicLib.SkillTag;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;

@SkillTag
public class AuxiliarBarrier extends SkillHandler<AttackSkillResult> implements Listener {

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata arg0) {
        return new AttackSkillResult(arg0);
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata arg1) {
    }

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    public NamespacedKey getKey() {
        return new NamespacedKey("realmcraft", getId().toLowerCase());
    }

    /*
     * Al bajar la vida a menos de 30% y realizar un ataque recibes tu y tu aliado
     * mas cercano que menos porcentaje de vida tenga, un escudo equivalente al 20%
     * de vida maxima de cada uno
     * Cooldown: 90s
     */

    public AuxiliarBarrier() {
        super(false);
        registerModifiers("low-health", "cooldown", "shield-percent", "team-distance");
    }

    private Map<UUID, Integer> cooldown = new HashMap<UUID, Integer>();
    private Map<UUID, Integer> shield = new HashMap<UUID, Integer>();

    @EventHandler
    public void onAttack(PlayerAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!MMOPlayerData.has(player))
                return;

            MMOPlayerData playerData = MMOPlayerData.get(player);

            if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                double lowHealth = meta.getModifier("low-health");
                double cooldown = meta.getModifier("cooldown");
                double shieldPercent = meta.getModifier("shield-percent");
                double teamDistance = meta.getModifier("team-distance");

                if (player.getHealth() / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() <= lowHealth) {
                    if (this.cooldown.containsKey(player.getUniqueId())) {
                        return;
                    }
                    if (this.shield.containsKey(player.getUniqueId())) {
                        if (this.shield.get(player.getUniqueId()) > 0) {
                            return;
                        }
                    }
                    this.cooldown.put(player.getUniqueId(), (int) cooldown);
                    this.shield.put(player.getUniqueId(),
                            (int) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * shieldPercent / 100));
                    // get nearby team mate with lowest health
                    RealPlayer realPlayer = RealPlayer.of(player);
                    Player lowest = null;
                    for (Player p : player.getWorld().getPlayers()) {
                        if (p.getUniqueId() != player.getUniqueId()) {
                            if (realPlayer.isTeam(player)) {
                                if (player.getLocation().distance(p.getLocation()) <= teamDistance) {
                                    if (this.shield.containsKey(p.getUniqueId())) {
                                        if (this.shield.get(p.getUniqueId()) > 0) {
                                            continue;
                                        }
                                    } else {
                                        if (lowest == null) {
                                            lowest = p;
                                        } else {
                                            if (lowest.getHealth() > p.getHealth()) {
                                                lowest = p;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (lowest != null) {
                        this.shield.put(lowest.getUniqueId(),
                                (int) (lowest.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * shieldPercent
                                        / 100));
                        RealMessage.sendActionBar(lowest, "<#FFBF00>Barrera auxiliar activada");
                    }
                    RealMessage.sendActionBar(player, "<#FFBF00>Barrera auxiliar activada");
                    Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
                        this.shield.remove(player.getUniqueId());
                    }, 20L * Double.valueOf(cooldown).longValue());
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (shield.containsKey(event.getEntity().getUniqueId())) {
            if (shield.get(event.getEntity().getUniqueId()) > 0) {
                double damage = event.getDamage();
                if (damage > shield.get(event.getEntity().getUniqueId())) {
                    damage -= shield.get(event.getEntity().getUniqueId());
                    shield.put(event.getEntity().getUniqueId(), 0);
                    event.setDamage(damage);
                } else {
                    shield.put(event.getEntity().getUniqueId(),
                            shield.get(event.getEntity().getUniqueId()) - (int) damage);
                    event.setDamage(0);
                }
            }
        }
    }
}
