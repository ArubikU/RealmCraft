package dev.arubik.realmcraft.Managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;

import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveFolder;
import dev.arubik.realmcraft.FileManagement.InteractiveFile.FileType;
import dev.arubik.realmcraft.Handlers.RealMessage;
import net.md_5.bungee.api.chat.hover.content.Item;

public class FileReader {
    private InteractiveFolder folder;
    private InteractiveFolder textures;
    private Plugin plugin;

    private InteractiveFile ItemCache;
    private static List<String> mainSchema = new ArrayList<String>();
    static {
        mainSchema.add("properties.namespace");
    }

    public FileReader(Plugin plugin) {
        this.plugin = plugin;
        folder = new InteractiveFolder("data", plugin);
        textures = new InteractiveFolder("textures", plugin);
        // ItemCache = new InteractiveFile("cache/items.json", plugin);

    }

    List<InteractiveFile> validConfigFiles = new ArrayList<InteractiveFile>();

    public void load() {
        folder.create();
        textures.create();
        for (InteractiveFolder subFolders : folder.getSubFolders()) {
            for (InteractiveFile file : subFolders.getFiles().values()) {
                if (file.getType() == FileType.UNKNOWN) {
                    continue;
                }
                SchemaValidator validator = new SchemaValidator(file);
                validator.setSchema(mainSchema);
                if (!validator.validate()) {
                    try {
                        throw new SchemaValidator.InvalidSchema("properties.namespace");
                    } catch (SchemaValidator.InvalidSchema e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                validConfigFiles.add(file);
            }
        }
    }
}
