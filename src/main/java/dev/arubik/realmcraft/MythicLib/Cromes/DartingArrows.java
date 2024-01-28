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
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.api.event.PlayerCombatEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.MMOItemsAPI;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import dev.arubik.realmcraft.MythicLib.SkillTag;

@SkillTag
public class DartingArrows extends SkillHandler<AttackSkillResult> implements Listener {

    private static Map<UUID, Integer> combat = new HashMap<UUID, Integer>();
    private static Map<UUID, UUID> stats = new HashMap<UUID, UUID>();

    @EventHandler
    public void joinCombat(PlayerCombatEvent event) {

        if (combat.containsKey(event.getPlayer().getUniqueId()) && event.entersCombat() == false) {
            combat.remove(event.getPlayer().getUniqueId());
        }
    }

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    public NamespacedKey getKey() {
        return new NamespacedKey("realmcraft", getId().toLowerCase());
    }

    public DartingArrows() {
        super(false);
        registerModifiers("streak", "min_dmg", "max_dmg");
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
        net.Indyuce.mmocore.api.player.PlayerData playerData2 = net.Indyuce.mmocore.api.player.PlayerData.get(player);
        if (playerData.getPassiveSkillMap().getSkill(this) != null) {
            if (new CustomEvent(player, getLowerCaseId()).callEvent()) {

                Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();

                if (combat.containsKey(player.getUniqueId())) {
                    if (combat.get(player.getUniqueId()) >= meta.getModifier("streak")) {
                        Double mindmg = meta.getModifier("min_dmg");
                        Double maxdmg = meta.getModifier("max_dmg");

                        double basedmg = event.getAttack().getDamage().getDamage();
                        double dmg = (Utils.random(mindmg, maxdmg) / 100)
                                * basedmg;
                        event.getAttack().getDamage().add(dmg, DamageType.SKILL);
                        RealMessage.sendRaw("Limit of streak reached player : " + player.getName() + " streak : "
                                + combat.get(player.getUniqueId()));
                        return;
                    }
                    if (new CustomEvent(player, getLowerCaseId() + "_FIRED").callEvent()) {
                        combat.put(player.getUniqueId(), combat.get(player.getUniqueId()) + 1);
                        RealMessage.PopUp(player,
                                "<#FFBB00>Flechas de Celo <green>+racha " + combat.get(player.getUniqueId()));
                        RealMessage.sendRaw("Effectuated dmg dmg damage player : " + player.getName() + " streak : "
                                + combat.get(player.getUniqueId()));
                    }
                }

            }
        }
    }

}
