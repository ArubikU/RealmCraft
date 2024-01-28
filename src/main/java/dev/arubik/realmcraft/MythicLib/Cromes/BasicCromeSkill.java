package dev.arubik.realmcraft.MythicLib.Cromes;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;

public abstract class BasicCromeSkill extends SkillHandler<AttackSkillResult> implements Listener {

    public BasicCromeSkill(boolean b) {
        super(b);
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

}
