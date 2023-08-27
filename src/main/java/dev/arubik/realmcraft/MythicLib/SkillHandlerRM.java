package dev.arubik.realmcraft.MythicLib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import dev.arubik.realmcraft.MythicLib.Passive.ComboAttack;
import dev.arubik.realmcraft.MythicLib.Passive.Daybreaker;
import dev.arubik.realmcraft.MythicLib.Passive.Flare;
import dev.arubik.realmcraft.MythicLib.Passive.Frostie;
import dev.arubik.realmcraft.MythicLib.Passive.LavaInmunity;
import dev.arubik.realmcraft.MythicLib.Passive.NaturalRegen;
import dev.arubik.realmcraft.MythicLib.Passive.NightHug;
import dev.arubik.realmcraft.MythicLib.Passive.RegenChance;
import dev.arubik.realmcraft.MythicLib.Passive.SoulCollector;
import dev.arubik.realmcraft.MythicLib.Passive.StackedAttack;
import dev.arubik.realmcraft.MythicLib.Passive.WindMaestery;
import dev.arubik.realmcraft.MythicLib.Passive.WitherEffectInvulnerability;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.manager.SkillManager;
import net.Indyuce.mmoitems.util.MMOUtils;
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

public class SkillHandlerRM implements Depend, Listener {
    @Getter
    private static StackedAttack stackedAttack;
    @Getter
    private static RegenChance regenChance;
    @Getter
    private static ComboAttack comboAttack;
    @Getter
    private static MythicPlaceholders mythicPlaceholders;

    public static SkillHandler[] handlers;

    public static void register() {
        MythicPlaceholders mythicPlaceholders = new MythicPlaceholders();
        mythicPlaceholders.register();

        RealMessage.sendConsoleMessage("&aRegistering skill handlers...");
        handlers = new SkillHandler[] { new StackedAttack(), new RegenChance(), new ComboAttack(),
                new Flare(), new Frostie(), new NaturalRegen(), new NightHug(), new WindMaestery(),
                new WitherEffectInvulnerability(), new SoulCollector(), new Daybreaker(), new LavaInmunity() };
        for (SkillHandler handler : handlers) {
            try {

                MythicLib.plugin.getSkills().registerSkillHandler(handler);
                if (handler instanceof Listener)
                    Bukkit.getPluginManager().registerEvents((Listener) handler, realmcraft.getInstance());
                RealMessage.sendConsoleMessage("&aRegistered skill handler: " + handler.getId());
            } catch (Throwable t) {
                RealMessage.sendConsoleMessage(
                        "&cCould not register skill handler: " + handler.getId() + " &c" + t.getMessage());
            }
        }

        SkillHandlerRM.MMOCoreInitialize();
        SkillHandlerRM.MMOItemsInitialize();
    }

    public static void MMOCoreInitialize() {

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

    public static void MMOItemsInitialize() {

        SkillManager mmoitems = net.Indyuce.mmoitems.MMOItems.plugin.getSkills();

        Map<String, net.Indyuce.mmoitems.skill.RegisteredSkill> skills = new HashMap<>();

        Class<?> mmoitemsskillclass = mmoitems.getClass();
        Field field = null;
        try {
            field = mmoitemsskillclass.getDeclaredField("skills");
            field.setAccessible(true);
            skills = (Map<String, net.Indyuce.mmoitems.skill.RegisteredSkill>) field.get(mmoitems);
        } catch (NoSuchFieldException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        File skillFolder = new File(MMOItems.plugin.getDataFolder() + "/skill");
        if (!skillFolder.exists()) {

            try {

                // Create folder
                skillFolder.mkdir();

                // Copy example skills
                for (SkillHandler handler : MythicLib.plugin.getSkills().getHandlers()) {
                    InputStream res = MMOItems.plugin.getResource("default/skill/" + handler.getLowerCaseId() + ".yml");
                    if (res != null)
                        Files.copy(res,
                                new File(
                                        MMOItems.plugin.getDataFolder() + "/skill/" + handler.getLowerCaseId() + ".yml")
                                        .getAbsoluteFile().toPath());
                }

                // Should not happen
            } catch (IOException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING,
                        "Could not save default ability configs: " + exception.getMessage());
            }
        }

        // Copy MythicLib skills
        for (SkillHandler<?> handler : MythicLib.plugin.getSkills().getHandlers()) {
            net.Indyuce.mmoitems.api.ConfigFile config = new net.Indyuce.mmoitems.api.ConfigFile("/skill",
                    handler.getLowerCaseId());
            if (!config.exists()) {
                config.getConfig().set("name",
                        MMOUtils.caseOnWords(handler.getId().replace("_", " ").replace("-", " ").toLowerCase()));
                for (String mod : handler.getModifiers()) {
                    config.getConfig().set("modifier." + mod + ".name",
                            MMOUtils.caseOnWords(mod.replace("-", " ").toLowerCase()));
                    config.getConfig().set("modifier." + mod + ".default-value", 0);
                }
                config.save();
            }

            try {

                // Attempt to register
                skills.put(handler.getId(),
                        new net.Indyuce.mmoitems.skill.RegisteredSkill(handler, config.getConfig()));

                // Fail
            } catch (RuntimeException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING,
                        "Could not load skill '" + handler.getId() + "': " + exception.getMessage());
            }
        }

        try {
            field.set(mmoitems, skills);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MythicLib" };
    }
}
