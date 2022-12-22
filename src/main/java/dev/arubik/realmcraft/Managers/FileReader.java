package dev.arubik.realmcraft.Managers;

import org.bukkit.plugin.Plugin;

import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveFolder;
import net.md_5.bungee.api.chat.hover.content.Item;

public class FileReader {
    private InteractiveFolder folder;
    private Plugin plugin;

    private InteractiveFile ItemCache;

    public FileReader(Plugin plugin) {
        this.plugin = plugin;
        folder = new InteractiveFolder("data");
        ItemCache = new InteractiveFile("cache/items.json");

    }

    public void load() {
        ItemCache.setAutoUpdate(true);
        ItemCache.set("test.value.org", "Dump!?");
    }
}
