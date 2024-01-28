package dev.arubik.realmcraft.MythicLib.Cromes;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Api.Events.CustomEvent;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BrighterThanSun extends SkillHandler<AttackSkillResult> implements Listener {

    private static Map<UUID, Integer> dragonCallStacks = new HashMap<>();
    private static Set<UUID> players = new HashSet<>();

    public BrighterThanSun() {
        super(false);
        registerModifiers("min_bonus_stack_damage", "max_bonus_stack_damage", "min_bonus_mana_gain",
                "max_bonus_mana_gain",
                "max_stacks", "min_final_stack_damage", "max_final_stack_damage", "sun_cooldown");

        Bukkit.getScheduler().runTaskTimer(realmcraft.getInstance(), () -> ManaRunnable(), 0, 20);
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
            // double minBonusManaGain = metaSkill.getModifier("min_bonus_mana_gain");
            // double maxBonusManaGain = metaSkill.getModifier("max_bonus_mana_gain");
            int maxStacks = (int) metaSkill.getModifier("max_stacks");
            if (!players.contains(player.getUniqueId())) {
                if (!dragonCallStacks.containsKey(player.getUniqueId())
                        || dragonCallStacks.get(player.getUniqueId()) < 1) {
                    dragonCallStacks.put(player.getUniqueId(), 1);
                }
                if (dragonCallStacks.containsKey(player.getUniqueId())) {
                    dragonCallStacks.put(player.getUniqueId(), dragonCallStacks.get(player.getUniqueId()) + 1);
                    RealMessage.PopUp(player, "<#FFBB00>Llamado del Dragon <green>+lote "
                            + dragonCallStacks.get(player.getUniqueId()) + "/" + maxStacks);
                }
                if (dragonCallStacks.get(player.getUniqueId()) > maxStacks) {
                    double minFinalStackDamage = metaSkill.getModifier("min_final_stack_damage");
                    double maxFinalStackDamage = metaSkill.getModifier("max_final_stack_damage");
                    double sunCooldown = metaSkill.getModifier("sun_cooldown");
                    // Calcular y aplicar el daño adicional por alcanzar el máximo de pilas
                    double finalDamageIncrease = Utils
                            .roundDouble(Utils.random(minFinalStackDamage, maxFinalStackDamage), 2);
                    double damage = event.getAttack().getDamage().getDamage() * finalDamageIncrease / 100;
                    event.getAttack().getDamage().add(damage, DamageType.SKILL);
                    players.add(player.getUniqueId());
                    dragonCallStacks.put(player.getUniqueId(), 0);
                    RealMessage.PopUp(player,
                            "<#FFBB00>Llamado del Dragon <green>Lote final <red>" + finalDamageIncrease
                                    + "%");
                    Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
                        players.remove(player.getUniqueId());
                    }, (long) (sunCooldown * 20));

                } else {
                    double minBonusStackDamage = metaSkill.getModifier("min_bonus_stack_damage");
                    double maxBonusStackDamage = metaSkill.getModifier("max_bonus_stack_damage");

                    double damageIncrease = 0;
                    for (int i = 0; i < dragonCallStacks.get(player.getUniqueId()); i++) {
                        damageIncrease += Utils
                                .roundDouble(Utils.random(minBonusStackDamage, maxBonusStackDamage), 2);
                    }
                    double damage = event.getAttack().getDamage().getDamage() * damageIncrease / 100;
                    event.getAttack().getDamage().add(damage, DamageType.SKILL);

                }
            }
        }
    }

    public void ManaRunnable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (dragonCallStacks.containsKey(player.getUniqueId()) && dragonCallStacks.get(player.getUniqueId()) > 0) {
                if (!MMOPlayerData.has(player))
                    return;

                MMOPlayerData playerData = MMOPlayerData.get(player);
                if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                    Skill metaSkill = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();

                    double minBonusMana = metaSkill.getModifier("min_bonus_mana_gain");
                    double maxBonusMana = metaSkill.getModifier("max_bonus_mana_gain");

                    double manaGain = 0;
                    for (int i = 0; i < dragonCallStacks.get(player.getUniqueId()); i++) {
                        manaGain += Utils
                                .roundDouble(Utils.random(minBonusMana, maxBonusMana), 2);
                    }

                    PlayerData playerDataM = PlayerData.get(player.getUniqueId());
                    Double maxMana = playerDataM.getStats().getInstance(StatType.MAX_MANA).getTotal();
                    if (playerDataM.getMana() + manaGain > maxMana) {
                        playerDataM.setMana(maxMana);
                    } else {
                        playerDataM.giveMana(manaGain, UpdateReason.OTHER);
                    }
                }
            }
        }
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata arg1) {
    }

}
