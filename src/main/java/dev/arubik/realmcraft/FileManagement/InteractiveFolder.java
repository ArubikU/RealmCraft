package dev.arubik.realmcraft.FileManagement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

public class InteractiveFolder {
    private static JavaPlugin plugin;
    private String path;

    public InteractiveFolder(String path) {
        this.path = path;
    }

    public static void setPlugin(JavaPlugin plugin) {
        InteractiveFolder.plugin = plugin;
    }

    public Map<String, InteractiveFile> getFiles() {
        Map<String, InteractiveFile> files = new HashMap<String, InteractiveFile>();
        File folder = new File(plugin.getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                files.put(file.getName(), new InteractiveFile(path + File.pathSeparator + file.getName()));
            }
        }
        return files;
    }

    public Map<String, String> getFolders() {
        Map<String, String> folders = new HashMap<String, String>();
        File folder = new File(plugin.getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                folders.put(file.getName(), path + File.pathSeparator + file.getName());
            }
        }
        return folders;
    }

    public String getSimplePath() {
        File folder = new File(plugin.getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder.getName();
    }

    public List<InteractiveFolder> getSubFolders() {
        List<InteractiveFolder> folders = new ArrayList<InteractiveFolder>();
        File folder = new File(plugin.getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                folders.add(new InteractiveFolder(path + File.pathSeparator + file.getName()));
            }
        }
        return folders;
    }

}
