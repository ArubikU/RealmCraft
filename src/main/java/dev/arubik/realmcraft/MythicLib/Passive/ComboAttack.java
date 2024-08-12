package dev.arubik.realmcraft.MythicLib.Passive;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.MythicLib.SkillTag;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import lombok.Getter;

@SkillTag
public class ComboAttack extends SkillHandler<AttackSkillResult> implements Listener {

  @Override
  public String getLowerCaseId() {
    return getId().toLowerCase() + "_PRIVATESKILL";
  }

  public ComboAttack() {
    super(false);
    registerModifiers("damage-multiplier", "max-combo", "delay-remove");
  }

  @Override
  public @NotNull AttackSkillResult getResult(SkillMetadata meta) {
    return new AttackSkillResult(meta);
  }

  @Getter
  private static Map<UUID, Integer> ComboMap = new HashMap<>();

  @Override
  public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {

  }

  public void cast(LivingEntity target, Player player, AttackMetadata metadata) {
    if (!MMOPlayerData.has(player))
      return;
    MMOPlayerData playerData = MMOPlayerData.get(player);

    Skill skillMeta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
    if (target == null)
      return;
    final double damageMultiplier = skillMeta.getModifier("damage-multiplier");
    final int maxCombo = new Double(skillMeta.getModifier("max-combo")).intValue();
    if (ComboMap.containsKey(player.getUniqueId())) {
      int currentCombo = ComboMap.get(player.getUniqueId());
      if (!(currentCombo >= maxCombo)) {
        ComboMap.put(player.getUniqueId(), currentCombo + 1);
        return;
      }
    } else {
      ComboMap.put(player.getUniqueId(), 1);
      return;
    }
    int currentCombo = ComboMap.get(player.getUniqueId());
    if (currentCombo >= maxCombo) {
      double damage = metadata.getDamage().getDamage();
      while (currentCombo > 0) {
        damage += damage * damageMultiplier;
        currentCombo--;
      }
      metadata.getDamage().add(damage, io.lumine.mythic.lib.damage.DamageType.WEAPON);
      target.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 10);
      target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
    }
    Long delayRemove = new Double(skillMeta.getModifier("delay-remove") * 20).longValue();
    Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("RealmCraft"), new Runnable() {
      @Override
      public void run() {
        if (ComboMap.containsKey(player.getUniqueId())) {
          if (ComboMap.get(player.getUniqueId()) - 1 <= 0) {
            ComboMap.remove(player.getUniqueId());
            return;
          }
          ComboMap.put(player.getUniqueId(), ComboMap.get(player.getUniqueId()) - 1);
        }
      }
    }, delayRemove);

  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void a(PlayerAttackEvent event) {
    MMOPlayerData data = event.getAttacker().getData();
    LivingEntity target = event.getEntity();
    PassiveSkill skill = data.getPassiveSkillMap().getSkill(this);
    if (skill == null)
      return;

    this.cast(target, data.getPlayer(), event.getAttack());
  }

}
