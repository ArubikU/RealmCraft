package dev.arubik.realmcraft.MythicMobs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.arubik.realmcraft.EmoteHandler.EmoteMain;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.MMOCoreAPI;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import xyz.larkyy.aquaticmodelengine.api.model.animation.LoopMode;

public class PlayerEmote implements ITargetedEntitySkill, Depend {

    private String url = "https://s.namemc.com/i/7dd726b81e42eccf.png";
    private Boolean idle = false;
    private Boolean head = true;
    private LoopMode loop = LoopMode.ONCE;
    private Boolean Slim = false;
    private String animation = "idle";

    public PlayerEmote(MythicLineConfig config) {

        idle = config.getBoolean("idle", false);
        head = config.getBoolean("head", true);
        Slim = config.getBoolean("slim", false);
        animation = config.getString("animation", "idle");
        url = config.getString("url", "https://s.namemc.com/i/7dd726b81e42eccf.png");
        String lmode = config.getString("loop", "once");
        if (lmode.equalsIgnoreCase("once")) {
            loop = LoopMode.ONCE;
        } else if (lmode.equalsIgnoreCase("loop")) {
            loop = LoopMode.LOOP;
        } else if (lmode.equalsIgnoreCase("hold")) {
            loop = LoopMode.HOLD;
        } else {
            loop = LoopMode.ONCE;
        }

    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        LivingEntity LivingMob = (LivingEntity) BukkitAdapter.adapt(target);
        if (idle) {
            EmoteMain.playAnimationMOBEndIdle(LivingMob, animation, head, url, Slim, loop);
        } else {
            EmoteMain.playAnimationMOB(LivingMob, animation, head, url, Slim, loop);
        }
        return SkillResult.SUCCESS;
    }

    @Override
    public String[] getDependatsPlugins() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDependatsPlugins'");
    }
}