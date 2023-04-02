package dev.arubik.realmcraft.MythicLib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.bukkit.Bukkit;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import dev.arubik.realmcraft.MythicLib.Passive.ComboAttack;
import dev.arubik.realmcraft.MythicLib.Passive.RegenChance;
import dev.arubik.realmcraft.MythicLib.Passive.StackedAttack;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.skill.RegisteredSkill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class SkillHandlerRM implements Depend {
    @Getter
    private static StackedAttack stackedAttack;
    @Getter
    private static RegenChance regenChance;
    @Getter
    private static ComboAttack comboAttack;
    @Getter
    private static MythicPlaceholders mythicPlaceholders;

    public static void register() {
        MythicPlaceholders mythicPlaceholders = new MythicPlaceholders();
        mythicPlaceholders.register();
        try {
            stackedAttack = new StackedAttack();
            MythicLib.plugin.getSkills().registerSkillHandler(stackedAttack);
            Bukkit.getPluginManager().registerEvents(stackedAttack, realmcraft.getInstance());
            RealMessage.sendConsoleMessage("MythicLib StackedAttack pasive Skill registered");
        } catch (Throwable e) {
            RealMessage.sendConsoleMessage("MythicLib StackedAttack pasive Skill not registered or already registered");
        }
        try {
            regenChance = new RegenChance();
            MythicLib.plugin.getSkills().registerSkillHandler(regenChance);
            Bukkit.getPluginManager().registerEvents(regenChance, realmcraft.getInstance());
            RealMessage.sendConsoleMessage("MythicLib RegenChance pasive Skill registered");
        } catch (Throwable e) {
            RealMessage.sendConsoleMessage("MythicLib RegenChance pasive Skill not registered or already registered");
        }
        try {
            comboAttack = new ComboAttack();
            MythicLib.plugin.getSkills().registerSkillHandler(comboAttack);
            Bukkit.getPluginManager().registerEvents(comboAttack, realmcraft.getInstance());
            RealMessage.sendConsoleMessage("MythicLib ComboAttack pasive Skill registered");
        } catch (Throwable e) {
            RealMessage.sendConsoleMessage("MythicLib ComboAttack pasive Skill not registered or already registered");
        }
        SkillHandlerRM.Initialize();
    }

    public static void Initialize() {

        net.Indyuce.mmocore.manager.SkillManager mmocore = net.Indyuce.mmocore.MMOCore.plugin.skillManager;
        // Check for default files
        File skillFolder = new File(MMOCore.plugin.getDataFolder() + "/skills");
        if (!skillFolder.exists())
            try {
                skillFolder.mkdir();

                for (SkillHandler handler : MythicLib.plugin.getSkills().getHandlers()) {
                    InputStream res = MMOCore.plugin.getResource("default/skills/" + handler.getLowerCaseId() + ".yml");
                    if (res != null)
                        Files.copy(res,
                                new File(
                                        MMOCore.plugin.getDataFolder() + "/skills/" + handler.getLowerCaseId() + ".yml")
                                        .getAbsoluteFile().toPath());
                }
            } catch (IOException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING,
                        "Could not save default skill configs: " + exception.getMessage());
            }

        for (SkillHandler handler : MythicLib.plugin.getSkills().getHandlers()) {

            // Check if config file exists
            ConfigFile config = new ConfigFile("/skills", handler.getLowerCaseId());
            if (!config.exists()) {
                config.getConfig().set("name",
                        MMOCoreUtils.caseOnWords(handler.getId().replace("_", " ").replace("-", " ").toLowerCase()));
                config.getConfig().set("lore", Arrays.asList("This is the default skill description", "",
                        "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}"));
                config.getConfig().set("material", "BOOK");
                for (Object mod : handler.getModifiers()) {
                    config.getConfig().set(mod + ".base", 0);
                    config.getConfig().set(mod + ".per-level", 0);
                    config.getConfig().set(mod + ".min", 0);
                    config.getConfig().set(mod + ".max", 0);
                }
                config.save();
            }

            try {
                final RegisteredSkill skill = new RegisteredSkill(handler, config.getConfig());
                mmocore.registerSkill(skill);
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING,
                        "Could not load skill '" + handler.getId() + "': " + exception.getMessage());
            }
        }

    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MythicLib" };
    }
}
