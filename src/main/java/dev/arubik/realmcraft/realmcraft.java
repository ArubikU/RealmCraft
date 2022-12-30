package dev.arubik.realmcraft;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.DefaultConfigs.MainConfig;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveFolder;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.FileReader;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class realmcraft extends JavaPlugin {

    private MiniMessage mm = MiniMessage.miniMessage();
    private LegacyComponentSerializer lcs = LegacyComponentSerializer.legacySection();
    private FileReader fileReader;
    public static String separator = "/";
    @Getter
    private static realmcraft instance;

    @Getter
    private static InteractiveFile InteractiveConfig;

    @Override
    public void onEnable() {
        instance = this;
        InteractiveFile.setPlugin(this);
        InteractiveFolder.setPlugin(this);
        fileReader = new FileReader(this);
        reloadConfig();
        reload();
        RealMessage.sendConsoleMessage("<yellow>RealmCraft has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reload() {
        fileReader.load();
    }

    public void reloadConfig() {
        InteractiveConfig = new InteractiveFile("config.yml");
        InteractiveConfig.create();
        InteractiveConfig.loadDefaults(new MainConfig());
        separator = InteractiveConfig.getString("file.separator");
    }

    public static @NotNull MiniMessage getMiniMessage() {
        return MiniMessage.miniMessage();
    }

    public static @NotNull LegacyComponentSerializer getLegacyComponentSerializer() {
        return LegacyComponentSerializer.legacySection();
    }
}
