package dev.arubik.realmcraft;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.FileReader;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class realmcraft extends JavaPlugin {

    private InteractiveFile mainConfig;
    private MiniMessage mm = MiniMessage.miniMessage();
    private LegacyComponentSerializer lcs = LegacyComponentSerializer.legacySection();
    private FileReader fileReader;
    @Getter
    private static realmcraft instance;

    @Override
    public void onEnable() {
        InteractiveFile.setPlugin(this);
        fileReader = new FileReader(this);
        // Plugin startup logic
        instance = this;

        RealMessage.sendConsoleMessage("&aRealmCraft has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reload() {
        fileReader.load();
    }

    public void reloadConfig() {
        mainConfig = new InteractiveFile("config.yml");
    }

    public static @NotNull MiniMessage getMiniMessage() {
        return MiniMessage.miniMessage();
    }

    public static @NotNull LegacyComponentSerializer getLegacyComponentSerializer() {
        return LegacyComponentSerializer.legacySection();
    }
}
