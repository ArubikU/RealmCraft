package dev.arubik.realmcraft;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.EntityHider;
import dev.arubik.realmcraft.Api.Events.LoreEvent;
import dev.arubik.realmcraft.Api.Listeners.AttackListener;
import dev.arubik.realmcraft.Api.Listeners.ChangeGamemode;
import dev.arubik.realmcraft.CustomSouds.WeaponListener;
import dev.arubik.realmcraft.DefaultConfigs.LangConfig;
import dev.arubik.realmcraft.DefaultConfigs.MainConfig;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.Handlers.PlaceholderConfigParser;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.IReplacer.IReplacer;
import dev.arubik.realmcraft.IReplacer.IReplacerListener;
import dev.arubik.realmcraft.IReplacer.IReplacerModifier;
import dev.arubik.realmcraft.LootGen.ContainerApi;
import dev.arubik.realmcraft.MMOItems.CustomLore;
import dev.arubik.realmcraft.MMOItems.Durability.DurabilityLore;
import dev.arubik.realmcraft.MMOItems.Durability.EnablePlaceholders;
import dev.arubik.realmcraft.MMOItems.Durability.MMOListener;
import dev.arubik.realmcraft.MMOItems.Durability.MinimessageFormat;
import dev.arubik.realmcraft.MMOItems.Durability.PlaceholderEnableLore;
import dev.arubik.realmcraft.MMOItems.Durability.RepairMaterial;
import dev.arubik.realmcraft.MMOItems.Durability.RestoreStellium;
import dev.arubik.realmcraft.MMOItems.Range.InteractAnimation;
import dev.arubik.realmcraft.MMOItems.Range.NBTCompund;
import dev.arubik.realmcraft.MMOItems.Range.RangeListener;
import dev.arubik.realmcraft.Managers.Depend;
import dev.arubik.realmcraft.Managers.FileReader;
import dev.arubik.realmcraft.Managers.Command.CommandMapper;
import dev.arubik.realmcraft.Managers.Command.RealmcraftCommand;
import dev.arubik.realmcraft.MythicLib.SkillHandlerRM;
import dev.arubik.realmcraft.MythicMobs.LootBagListener;
import dev.arubik.realmcraft.MythicMobs.MythicListener;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class realmcraft extends JavaPlugin {
    public String separator = "/";
    @Getter
    private FileReader fileReader;
    @Getter
    private IReplacer replacer;
    @Getter
    private CommandMapper commandMapper;
    @Getter
    private static realmcraft instance;
    @Getter
    private static InteractiveFile InteractiveConfig;

    @Getter
    private static InteractiveFile ContainerInstances;

    @Getter
    private static InteractiveFile MinecraftLang;
    @Getter
    private static EntityHider entityHider;

    @Override
    public void onEnable() {
        instance = this;
        entityHider = new EntityHider(this, EntityHider.Policy.BLACKLIST);
        fileReader = new FileReader(this);
        LoreEvent.reboot();
        reload();
        LoreEvent.registerListener();
        RealMessage.sendConsoleMessage("<yellow>RealmCraft has been enabled!");
        if (InteractiveConfig.getBoolean("mode.api", false))
            return;
        if (InteractiveConfig.getBoolean("modules.command", true))
            commandMapper = new CommandMapper("realmcraft", this);

        if (InteractiveConfig.getBoolean("modules.durability", true)) {
            MMOListener.register();
            DurabilityLore.register();
        }
        if (InteractiveConfig.getBoolean("modules.placeholder-lore", true)) {
            EnablePlaceholders.register();
            PlaceholderEnableLore.register();
        }
        if (InteractiveConfig.getBoolean("modules.lang-lore", false))
            MinimessageFormat.register();
        if (InteractiveConfig.getBoolean("modules.repair-material-stat", true))
            RepairMaterial.register();
        // register listeners
        if (InteractiveConfig.getBoolean("modules.attack-listeners", true))
            AttackListener.register();
        if (InteractiveConfig.getBoolean("modules.vanilla-fix", true))
            ChangeGamemode.register();
        if (InteractiveConfig.getBoolean("modules.mythic-mobs", true)) {
            MythicListener.register();
        }
        if (InteractiveConfig.getBoolean("modules.ireplacer", true)) {
            replacer = new IReplacer(this);
            try {
                IReplacerModifier.Register();
                RealMessage.sendConsoleMessage("<yellow>Registered IReplacerModifier");
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register IReplacerModifier");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.command-realmcraft", true)
                && InteractiveConfig.getBoolean("modules.command", true)) {
            try {
                RealmcraftCommand.Initialize();
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register RealmcraftCommand");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.ireplacer", true)) {
            try {
                replacer.setup();
                IReplacerListener.register();
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register IReplacer");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.mmocore-skills", true)) {
            try {
                SkillHandlerRM.register();
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register SkillHandlerRM");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.lootbag", true)) {
            try {
                LootBagListener.register();
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register Lootbag");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.rangelistener", true)) {
            try {
                RangeListener.register();
                NBTCompund.register();
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register RangeListener");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.interactionanimation", true)) {
            try {
                InteractAnimation.register();
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register InteractAnimation");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.customlore", true)) {
            try {
                CustomLore.register();
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register CustomLore");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.customsound", true)) {
            try {
                WeaponListener.register();
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register CustomSound");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.mmocoreextension", true)) {
            try {
                // MMOCore.plugin.loadManager.registerLoader(new Loader());
                RestoreStellium.register();
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register MMOCOREExtension");
                e.printStackTrace();
            }
        }
        if (InteractiveConfig.getBoolean("modules.containers", true)) {
            try {
                Bukkit.getServer().getPluginManager().registerEvents(new ContainerApi(), realmcraft.getInstance());
            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register Container");
                e.printStackTrace();
            }
        }

        // verify existence of me.aida.gpttalk.lib.openai.service.OpenAIService class
        if (InteractiveConfig.getBoolean("modules.gpttalk", true)) {
            try {
                Class.forName("me.aida.gpttalk.lib.openai.service.OpenAIService");
                // BASE_URL
                // get string field BASE_URL and replace with https://api.pawan.krd/v1
                Field field = Class.forName("me.aida.gpttalk.lib.openai.service.OpenAIService")
                        .getDeclaredField("BASE_URL");
                field.setAccessible(true);
                field.set(null, "https://api.pawan.krd/v1");

            } catch (Throwable e) {
                RealMessage.sendConsoleMessage("<red>Failed to register GPTTalk");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        LoreEvent.unregisterListener();
        entityHider.close();
        InteractiveFile.close();
        if (InteractiveConfig.getBoolean("mode.api", false))
            return;

        if (InteractiveConfig.getBoolean("modules.command-realmcraft", true)) {
            RealmcraftCommand.Uninitialize();
        }
    }

    public static enum Modules {
        command, durability, placeholder_lore, lang_lore, repair_material_stat, lore_listeners, attack_listeners,
        vanilla_fix, mythic_mobs, ireplacer, mmocore_skills, command_realmcraft;
    }

    public void reload() {
        reloadConfig();
        reloadReader();
    }

    public static Boolean LoreProtocolParser = false;
    public static Boolean ModifireProtocolParser = false;

    public void reloadReader() {
        fileReader.load();
    }

    public void reloadConfig() {
        InteractiveConfig = new InteractiveFile("config.yml", this);
        InteractiveConfig.create();
        InteractiveConfig.loadDefaults(new MainConfig());
        MinecraftLang = new InteractiveFile("lang.yml", this);
        MinecraftLang.create();
        MinecraftLang.loadLoader(new LangConfig());
        separator = InteractiveConfig.getString("file.separator");

        LoreProtocolParser = InteractiveConfig.getBoolean("lore-protocol-parser", true);
        ModifireProtocolParser = InteractiveConfig.getBoolean("modifier-protocol-parser", true);

        if (InteractiveConfig.getBoolean("modules.loot", true)) {
            ContainerInstances = new InteractiveFile("lootinstances.yml", this);
            ContainerInstances.create();
        }
    }

    public static Component getMiniMessage(String string) {
        string = PlaceholderConfigParser.parser(string);
        return MiniMessage.miniMessage().deserialize(string);
    }

}
