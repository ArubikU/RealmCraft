package dev.arubik.realmcraft.MythicLib.Cromes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import com.willfp.eco.core.events.EntityDeathByEntityEvent;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Api.Events.CustomEvent;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import net.Indyuce.mmocore.api.event.PlayerCombatEvent;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.MMOItemsAPI;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

import dev.arubik.realmcraft.MythicLib.SkillTag;

@SkillTag
public class Vacio extends SkillHandler<AttackSkillResult> implements Listener {

    private static Map<UUID, Integer> combat = new HashMap<UUID, Integer>();
    private static Map<UUID, UUID> stats = new HashMap<UUID, UUID>();

    @EventHandler
    public void joinCombat(PlayerCombatEvent event) {
        Player player = event.getPlayer();

        if (combat.containsKey(event.getPlayer().getUniqueId()) && event.entersCombat() == false) {
            combat.remove(event.getPlayer().getUniqueId());
        }
        if (!combat.containsKey(event.getPlayer().getUniqueId()) && event.entersCombat() == true) {
            MMOPlayerData playerData = MMOPlayerData.get(player);
            if (!MMOPlayerData.has(player))
                return;
            if (playerData.getPassiveSkillMap().getSkill(this) != null) {

                if (new CustomEvent(player, getLowerCaseId() + "_LOAD").callEvent()) {
                    Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                    combat.put(player.getUniqueId(), 0);

                    double mincrit = meta.getModifier("min_crit");
                    double maxcrit = meta.getModifier("max_crit");
                    double basecrit = playerData.getStatMap().getInstance("critical-strike-chance").getTotal();
                    double crit = (Utils.random(mincrit, maxcrit) / 100)
                            * basecrit;
                    // critical-strike-chance'

                    UUID randomUUID = UUID.randomUUID();

                    stats.put(player.getUniqueId(), randomUUID);

                    playerData.getStatMap().getInstance(StatType.CRITICAL_STRIKE_CHANCE.toString())
                            .addModifier(new StatModifier(randomUUID.toString(), "critical-strike-chance", crit,
                                    ModifierType.FLAT));
                    player.getPersistentDataContainer().set(getKey(), PersistentDataType.STRING, randomUUID.toString());

                    RealMessage
                            .sendRaw("Added crit chance : " + crit + " player : " + event.getPlayer().getName()
                                    + " streak : "
                                    + combat.get(player.getUniqueId()));
                }
            }
        }
    }

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    public NamespacedKey getKey() {
        return new NamespacedKey("realmcraft", getId().toLowerCase());
    }

    public Vacio() {
        super(false);
        registerModifiers("streak", "min_crit", "max_crit");
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata arg0) {
        return new AttackSkillResult(arg0);
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata meta) {
    }

    @EventHandler
    public void onPlayerAttack(PlayerAttackEvent event) {
        Player player = event.getAttacker().getPlayer();
        if (!MMOPlayerData.has(player))
            return;
        MMOPlayerData playerData = MMOPlayerData.get(player);
        if (playerData.getPassiveSkillMap().getSkill(this) != null) {
            if (new CustomEvent(player, getLowerCaseId()).callEvent()) {

                Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();

                if (combat.containsKey(player.getUniqueId())) {
                    if (combat.get(player.getUniqueId()) >= meta.getModifier("streak")) {
                        if (playerData.getStatMap().getInstance(StatType.CRITICAL_STRIKE_CHANCE.toString())
                                .contains(player.getUniqueId().toString())) {

                            player.getPersistentDataContainer().remove(getKey());
                            playerData.getStatMap().getInstance(StatType.CRITICAL_STRIKE_CHANCE.toString())
                                    .remove(stats.get(player.getUniqueId()).toString());
                            RealMessage.sendRaw("Limit of streak reached player : " + player.getName() + " streak : "
                                    + combat.get(player.getUniqueId()));
                            RealMessage.PopUp(player,
                                    "<#FFBB00>Vacio <green>Racha Terminada!");
                        }

                        return;
                    }
                    if (new CustomEvent(player, getLowerCaseId() + "_FIRED").callEvent()) {
                        combat.put(player.getUniqueId(), combat.get(player.getUniqueId()) + 1);
                        RealMessage.PopUp(player,
                                "<#FFBB00>Vacio <green>lote " + combat.get(player.getUniqueId()) + "/"
                                        + meta.getModifier("streak"));
                        RealMessage.sendRaw("Effectuated crit chance player : " + player.getName() + " streak : "
                                + combat.get(player.getUniqueId()));
                    }
                }

            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getPersistentDataContainer().has(getKey())) {
            UUID attributeUUID = UUID.fromString(
                    event.getPlayer().getPersistentDataContainer().get(getKey(), PersistentDataType.STRING));

            if (!MMOPlayerData.has(event.getPlayer()))
                return;
            MMOPlayerData playerData = MMOPlayerData.get(event.getPlayer());

            playerData.getStatMap().getInstance("critical-strike-chance")
                    .remove(attributeUUID.toString());

            event.getPlayer().getPersistentDataContainer().remove(getKey());

        }
    }
}
