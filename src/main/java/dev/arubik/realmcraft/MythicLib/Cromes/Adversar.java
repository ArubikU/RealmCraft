package dev.arubik.realmcraft.MythicLib.Cromes;

import java.util.HashSet;
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
import dev.arubik.realmcraft.MythicLib.SkillTag;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;

@SkillTag
public class Adversar extends SkillHandler<AttackSkillResult> implements Listener {

    private static Set<UUID> players = new HashSet<UUID>();

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    public NamespacedKey getKey() {
        return new NamespacedKey("realmcraft", getId().toLowerCase());
    }

    public Adversar() {
        super(false);
        registerModifiers("chance", "seconds", "min_speed", "max_speed");
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata arg0) {
        return new AttackSkillResult(arg0);
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata meta) {
    }

    @EventHandler
    public void onEntityKillByPlayer(EntityDeathByEntityEvent event) {
        if (event.getKiller() instanceof Player player) {
            if (!players.contains(player.getUniqueId())) {
                if (!MMOPlayerData.has(player))
                    return;
                MMOPlayerData playerData = MMOPlayerData.get(player);
                if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                    Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                    double chance = meta.getModifier("chance");
                    if (Utils.Chance(chance * 100, 100)) {
                        Double seconds = meta.getModifier("seconds") * 20;

                        double minspeed = meta.getModifier("min_speed");
                        double maxspeed = meta.getModifier("max_speed");
                        double speed = Utils.random(minspeed, maxspeed);
                        applyModifier(player, speed, seconds.longValue());
                    }

                }
            }
        }
    }

    public void applyModifier(Player player, Double percent, Long seconds) {

        if (new CustomEvent(player, getLowerCaseId()).callEvent()) {

            UUID randomUUID = UUID.randomUUID();
            Double def = percent;
            // TODO Add speed percent logic

            percent = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue() * (percent / 100);

            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(
                    new AttributeModifier(randomUUID, getId(), percent, Operation.ADD_NUMBER));

            // Set temporal data on player
            player.getPersistentDataContainer().set(getKey(), PersistentDataType.STRING, randomUUID.toString());

            RealMessage.sendActionBar(player, "<#FFBF00>Adversario > <white>Haz recibido <#FFBF00>" + def
                    + "%<white> de defensa por <#FFBF00>" + seconds / 20 + "<white> s");
            RealMessage.PopUp(player,
                    "<#FFBB00>Adversario <green>+spd" + def + "%<#6961d9>" + seconds / 20 + "s");
            Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
                player.getPersistentDataContainer().remove(getKey());
                players.remove(player.getUniqueId());
                for (AttributeModifier modifier : player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                        .getModifiers()) {
                    if (modifier.getUniqueId().compareTo(randomUUID) == 0) {
                        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(modifier);
                    }
                }
            }, seconds);
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getPersistentDataContainer().has(getKey())) {
            UUID attributeUUID = UUID.fromString(
                    event.getPlayer().getPersistentDataContainer().get(getKey(), PersistentDataType.STRING));

            for (AttributeModifier modifier : event.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                    .getModifiers()) {
                if (modifier.getUniqueId().compareTo(attributeUUID) == 0) {
                    event.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(modifier);
                }
            }

            event.getPlayer().getPersistentDataContainer().remove(getKey());

        }
    }
}
