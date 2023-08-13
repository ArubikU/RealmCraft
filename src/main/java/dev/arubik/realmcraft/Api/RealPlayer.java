package dev.arubik.realmcraft.Api;

import java.lang.reflect.Field;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class RealPlayer {
    Player player;

    public RealPlayer(Player player) {
        this.player = player;
    }

    public static RealPlayer of(Player player) {
        return new RealPlayer(player);
    }

    public boolean consumeItem(Material mat, int reamount) {
        if (player.getInventory().contains(mat, reamount)) {
            player.getInventory().iterator().forEachRemaining(item -> {
                int amount = reamount;
                if (item != null && item.getType() == mat) {
                    if (item.getAmount() > amount) {
                        item.setAmount(item.getAmount() - amount);
                        return;
                    } else {
                        amount -= item.getAmount();
                        item.setAmount(0);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public StatMap getStatMap() {
        return MMOPlayerData.get(player).getStatMap();
    }

    public SkillMetadata getSkillMetadata(Skill skill) {
        MMOPlayerData playerData = MMOPlayerData.get(player);
        SkillMetadata skilld = new SkillMetadata(skill, playerData);
        return skilld;
    }

    public void trigger(SkillHandler skill) {
        MMOPlayerData playerData = MMOPlayerData.get(player);
        PassiveSkill Passkill = playerData.getPassiveSkillMap().getSkill(skill);
        DamageMetadata damage = new DamageMetadata();
        AttackMetadata attack = new AttackMetadata(damage, playerData.getStatMap().cache(EquipmentSlot.MAIN_HAND));
        TriggerMetadata metatrigger = new TriggerMetadata(playerData.getStatMap().cache(EquipmentSlot.MAIN_HAND),
                attack,
                player);
        SkillResult result = Passkill.getTriggeredSkill()
                .cast(metatrigger);
        Class skillclass = result.getClass();
        Field attackMeta;
        try {
            attackMeta = skillclass.getDeclaredField("attackMeta");
            attackMeta.setAccessible(true);
            attackMeta.set(result, attack);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Field target = null;

        try {
            target = skillclass.getDeclaredField("target");
            target.setAccessible(true);
            target.set(result, (LivingEntity) player);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SkillMetadata meta = new SkillMetadata(Passkill.getTriggeredSkill(), playerData);
        skill.whenCast(result, meta);

    }

    public void trigger(SkillHandler skill, LivingEntity entity) {
        MMOPlayerData playerData = MMOPlayerData.get(player);
        PassiveSkill Passkill = playerData.getPassiveSkillMap().getSkill(skill);
        DamageMetadata damage = new DamageMetadata();
        AttackMetadata attack = new AttackMetadata(damage, playerData.getStatMap().cache(EquipmentSlot.MAIN_HAND));
        TriggerMetadata metatrigger = new TriggerMetadata(playerData.getStatMap().cache(EquipmentSlot.MAIN_HAND),
                attack,
                entity);
        SkillResult result = Passkill.getTriggeredSkill()
                .cast(metatrigger);
        Class skillclass = result.getClass();
        Field attackMeta;
        try {
            attackMeta = skillclass.getDeclaredField("attackMeta");
            attackMeta.setAccessible(true);
            attackMeta.set(result, attack);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Field target = null;

        try {
            target = skillclass.getDeclaredField("target");
            target.setAccessible(true);
            target.set(result, (LivingEntity) entity);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SkillMetadata meta = new SkillMetadata(Passkill.getTriggeredSkill(), playerData);
        skill.whenCast(result, meta);

    }

    public Player getPlayer() {
        return player;
    }

    public void DamageEntity(double d, String e, DamageType dt, LivingEntity... entities) {

        DamageMetadata damage = new DamageMetadata();
        damage.add(d, Element.valueOf(e), dt);
        AttackMetadata attack = new AttackMetadata(damage, getStatMap());
        for (LivingEntity target : entities) {
            MythicLib.plugin.getDamage().damage(attack, target, false);
        }

    }
}
