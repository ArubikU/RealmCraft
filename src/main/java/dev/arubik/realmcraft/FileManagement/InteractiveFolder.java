package dev.arubik.realmcraft.FileManagement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.Plugin;

import dev.arubik.realmcraft.realmcraft;

public class InteractiveFolder {
    private Plugin plugin;
    private String path;

    public InteractiveFolder(String path, Plugin plugin2) {
        this.path = path;
        this.plugin = plugin2;
    }

    public Map<String, InteractiveFile> getFiles() {
        Map<String, InteractiveFile> files = new HashMap<String, InteractiveFile>();
        File folder = new File(plugin.getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                files.put(file.getName(),
                        new InteractiveFile(path + realmcraft.getInstance().separator + file.getName(), plugin));
            }
        }
        return files;
    }

    public List<InteractiveFile> getSubFiles() {
        List<InteractiveFile> files = new ArrayList<InteractiveFile>();
        File folder = new File(plugin.getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                files.add(new InteractiveFile(path + realmcraft.getInstance().separator + file.getName(), plugin));
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
                folders.put(file.getName(), path + realmcraft.getInstance().separator + file.getName());
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
                folders.add(new InteractiveFolder(path + realmcraft.getInstance().separator + file.getName(), plugin));
            }
        }
        return folders;
    }

    public void create() {
        File folder = new File(plugin.getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public Boolean has(String name) {
        File folder = new File(plugin.getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        for (File file : folder.listFiles()) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return path;
    }
}
