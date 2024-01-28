package dev.arubik.realmcraft.MythicLib.Cromes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Api.Events.CustomEvent;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.MythicLib.SkillTag;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;

@SkillTag
public class Ambar extends SkillHandler<AttackSkillResult> implements Listener {

    public Set<UUID> playerCooldown = new HashSet<UUID>();

    public Ambar() {
        super(false);
        registerModifiers("health-percent", "cooling", "seconds", "min_def", "max_def");
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

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata meta) {
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {

            if (!MMOPlayerData.has(player))
                return;
            MMOPlayerData playerData = MMOPlayerData.get(player);
            PlayerData playerDataM = PlayerData.get(player);
            if (playerData.getPassiveSkillMap().getSkill(this) != null) {
                if (!playerCooldown.contains(player.getUniqueId())) {
                    if (player.getHealth() <= playerDataM.getStats().getMap()
                            .getInstance(StatType.MAX_HEALTH.toString()).getTotal() / 2) {
                        if (new CustomEvent(player, getLowerCaseId()).callEvent()) {

                            Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                            Double seconds = meta.getModifier("seconds") * 20;
                            Double cooling = meta.getModifier("cooling") * 20;

                            double mindef = meta.getModifier("min_def");
                            double maxdef = meta.getModifier("max_def");
                            double basedef = playerDataM.getStats().getMap().getInstance(StatType.DEFENSE.toString())
                                    .getTotal();
                            double def = (Utils.random(mindef, maxdef) / 100)
                                    * basedef;
                            def = Utils.roundDouble(def, 1);

                            UUID randomUUID = UUID.randomUUID();
                            playerDataM.getStats().getMap().getInstance(StatType.DEFENSE.toString())
                                    .addModifier(
                                            new StatModifier(randomUUID.toString(), StatType.DEFENSE.toString(), def,
                                                    ModifierType.FLAT));
                            player.getPersistentDataContainer().set(getKey(), PersistentDataType.STRING,
                                    randomUUID.toString());
                            // enviame una action bar con el prefix en color ambar que diga cuanto tiempo
                            // durara
                            // su defensa extra y cuanto recibe de defensa extra
                            RealMessage.sendActionBar(player, "<#FFBF00>Ambar > <white>Haz recibido <#FFBF00>" + def
                                    + "<white> de defensa por <#FFBF00>" + seconds / 20 + "<white> s");
                            RealMessage.PopUp(player,
                                    "<#FFBB00>Ambar <green>+def" + def + " <#6961d9>" + seconds / 20 + "s");

                            Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
                                playerDataM.getStats().getMap().getInstance(StatType.DEFENSE.toString())
                                        .remove(randomUUID.toString());
                                player.getPersistentDataContainer().remove(getKey());
                            }, seconds.longValue());
                            Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), () -> {
                                playerCooldown.remove(player.getUniqueId());
                            }, seconds.longValue() + cooling.longValue());
                        }
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

            PlayerData playerDataM = PlayerData.get(event.getPlayer());
            playerDataM.getStats().getMap().getInstance(StatType.DEFENSE.toString())
                    .remove(attributeUUID.toString());

            event.getPlayer().getPersistentDataContainer().remove(getKey());

        }
    }
}
