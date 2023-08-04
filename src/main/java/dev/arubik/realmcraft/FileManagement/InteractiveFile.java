package dev.arubik.realmcraft.FileManagement;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;

import static com.google.gson.JsonParser.*;

import dev.arubik.realmcraft.Api.YamlComentor;
import dev.arubik.realmcraft.Api.RealCache.RealCacheMap;
import dev.arubik.realmcraft.DefaultConfigs.RealLoader;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.JsonEditor;
import dev.arubik.realmcraft.Handlers.RealMessage;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InteractiveFile {

    public enum FileType {
        JSON, YAML, UNKNOWN
    }

    private Plugin plugin;

    private String Path;
    @Setter
    private boolean AutoUpdate;
    private Object json;
    private FileType type = FileType.UNKNOWN;
    private String name;
    private String extension;

    public FileType getType() {
        return type;
    }

    private RealCacheMap<String, Object> cache;

    public InteractiveFile(String path, Plugin plugin) {
        this.cache = new RealCacheMap<String, Object>();
        this.cache.setUpdateInterval(1200);
        this.cache.setRemoveInterval(2400);

        this.plugin = plugin;
        this.extension = path.substring(path.lastIndexOf("."));
        Path = path;
        name = path.substring(path.lastIndexOf("/") + 1);
        // verifiy if the plugin have a file with the same path in resources
        // if not, create a new file with the same path in resources
        if (!(new File(plugin.getDataFolder(), path).exists())) {
            if (plugin.getResource(path) != null) {
                plugin.saveResource(path, false);
            } else {
                create();
            }
        }
        // load the file
        switch (extension) {
            case "json":
            case ".json": {
                // read the file
                try {
                    File file = new File(plugin.getDataFolder(), path);
                    FileReader fileReader = new FileReader(file.getAbsolutePath());
                    json = JsonParser.parseReader(fileReader);
                    type = FileType.JSON;
                    fileReader.close();
                    return;
                } catch (JsonSyntaxException | IOException e) {
                    e.printStackTrace();
                }
                type = FileType.JSON;
                json = JsonBuilder.create().toJson();
                break;
            }
            case "yml":
            case "yaml":
            case ".yaml":
            case ".yml": {
                type = FileType.YAML;
                File f = new File(plugin.getDataFolder(), path);
                if (!f.exists()) {
                    f.getParentFile().mkdirs();
                    YamlConfiguration s = YamlConfiguration.loadConfiguration(f);
                    org.bukkit.configuration.file.FileConfiguration data = (org.bukkit.configuration.file.FileConfiguration) s;
                    data.options().copyDefaults(true);
                }
                YamlConfiguration s = YamlConfiguration.loadConfiguration(f);
                json = s;
                break;
            }
            default:
                break;
        }

    }

    public Object getFile() {
        return json;
    }

    public static void close() {
    }

    // generate a main getter if the file is a json
    public Object get(String path) {
        return get(path, Object.class);
    }

    public <T> T get(String path, @NotNull Class<T> itype) {

        if (cache.containsKey(path)) {
            return (T) cache.get(path);
        }

        if (type == FileType.JSON) {
            JsonElement a = ((JsonElement) json);
            if (a.getAsJsonObject().has(path)) {
                Object temp = a.getAsJsonObject().get(path);
                if (temp != null) {
                    cache.put(path, temp);
                }
                return (T) temp;
            }
            for (String s : path.split("\\.")) {
                a = a.getAsJsonObject().get(s);
            }

            if (a != null) {
                cache.put(path, a);
            }
            return (T) a;
        }
        if (type == FileType.YAML) {
            Object temp = ((FileConfiguration) json).get(path);
            if (temp != null) {
                cache.put(path, temp);
                return (T) temp;
            }
        }
        return null;
    }

    // generate all the setters and getters of info like getInteger, getBoolean,
    // getString, etc
    public Integer getInteger(String path) {
        return get(path, Integer.class);
    }

    public Integer getInteger(String path, Integer def) {
        if (get(path, Integer.class) != null) {
            return get(path, Integer.class);
        }
        return def;
    }

    public Integer getInteger(String... path) {
        for (String s : path) {
            if (getInteger(s) != 0) {
                return getInteger(s);
            }
        }
        return null;
    }

    public Integer getInteger(String[] path, Integer def) {
        for (String s : path) {
            if (getInteger(s) != 0) {
                return getInteger(s);
            }
        }
        return def;
    }

    public String getString(String path) {
        if (path == null) {
            return "";
        }
        return get(path).toString();
    }

    public String getString(String path, String def) {
        if (path == null) {
            return def;
        }
        if (get(path, String.class) != null) {
            return get(path, String.class);
        }
        return def;
    }

    public String getString(String... path) {
        for (String s : path) {
            if (getString(s) != null) {
                return getString(s);
            }
        }
        return null;
    }

    public String getString(String[] path, String def) {
        for (String s : path) {
            if (getString(s) != null) {
                return getString(s);
            }
        }
        return def;
    }

    public Boolean getBoolean(String path) {
        return getString(path).equalsIgnoreCase("True");
    }

    public Boolean getBoolean(String path, Boolean def) {
        if (get(path) != null) {
            return getBoolean(path);
        }
        return def;
    }

    public Boolean getBoolean(String... path) {
        for (String s : path) {
            if (getBoolean(s)) {
                return getBoolean(s);
            }
        }
        return null;
    }

    public Boolean getBoolean(String[] path, Boolean def) {
        for (String s : path) {
            if (getBoolean(s)) {
                return getBoolean(s);
            }
        }
        return def;
    }

    public Double getDouble(String path) {
        if (type == FileType.JSON) {
            return Double.parseDouble(getString(path));
        } else {
            return ((FileConfiguration) json).getDouble(path);
        }
    }

    public Double getDouble(String path, Double def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return Double.parseDouble(getString(path));
            }
            return def;
        } else {
            return ((FileConfiguration) json).getDouble(path, def);
        }
    }

    public Double getDouble(String... path) {
        for (String s : path) {
            if (getDouble(s) != 0) {
                return getDouble(s);
            }
        }
        return null;
    }

    public Double getDouble(String[] path, Double def) {
        for (String s : path) {
            if (getDouble(s) != 0) {
                return getDouble(s);
            }
        }
        return def;
    }

    public Long getLong(String path) {
        if (type == FileType.JSON) {
            return Long.parseLong(getString(path));
        } else {
            return ((FileConfiguration) json).getLong(path);
        }
    }

    public Long getLong(String path, Long def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return Long.parseLong(getString(path));
            }
            return def;
        } else {
            return ((FileConfiguration) json).getLong(path, def);
        }
    }

    public Long getLong(String... path) {
        for (String s : path) {
            if (getLong(s) != 0) {
                return getLong(s);
            }
        }
        return null;
    }

    public Long getLong(String[] path, Long def) {
        for (String s : path) {
            if (getLong(s) != 0) {
                return getLong(s);
            }
        }
        return def;
    }

    public List<String> getStringList(String path) {
        if (type == FileType.JSON) {
            return (List<String>) get(path);
        } else {
            return ((FileConfiguration) json).getStringList(path);
        }
    }

    public JsonElement getNative() {
        return (JsonElement) json;
    }

    public List<String> getStringList(String path, List<String> def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return (List<String>) get(path);
            }
            return def;
        } else {
            if (((FileConfiguration) json).getStringList(path) != null) {
                return ((FileConfiguration) json).getStringList(path);
            }
            return def;
        }
    }

    public List<String> getStringList(String... path) {
        for (String s : path) {
            if (getStringList(s) != null) {
                return getStringList(s);
            }
        }
        return null;
    }

    public List<String> getStringList(String[] path, List<String> def) {
        for (String s : path) {
            if (getStringList(s) != null) {
                return getStringList(s);
            }
        }
        return def;
    }

    public List<Integer> getIntegerList(String path) {
        if (type == FileType.JSON) {
            List<Integer> list = Lists.newArrayList();
            for (Object o : (List<?>) get(path)) {
                list.add(Integer.parseInt(o.toString()));
            }
            return list;
        } else {
            return ((FileConfiguration) json).getIntegerList(path);
        }
    }

    public List<Integer> getIntegerList(String path, List<Integer> def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                List<Integer> list = Lists.newArrayList();
                for (Object o : (List<?>) get(path)) {
                    list.add(Integer.parseInt(o.toString()));
                }
                return list;
            }
            return def;
        } else {
            if (((FileConfiguration) json).getIntegerList(path) != null) {
                return ((FileConfiguration) json).getIntegerList(path);
            }
            return def;
        }
    }

    public List<Integer> getIntegerList(String... path) {
        for (String s : path) {
            if (getIntegerList(s) != null) {
                return getIntegerList(s);
            }
        }
        return null;
    }

    public List<Integer> getIntegerList(String[] path, List<Integer> def) {
        for (String s : path) {
            if (getIntegerList(s) != null) {
                return getIntegerList(s);
            }
        }
        return def;
    }

    private void setInJson(String path, Object value) {
        // JsonObject obj = ((JsonElement) json).getAsJsonObject();
        // JsonPath jsonPath = JsonEditor.parsePath(path);
        // json = JsonEditor.setValue(jsonPath, value, obj);
        String[] args = path.split("\\.");
        // json = JsonEditor.set(args, value, ((JsonElement) json).getAsJsonObject());
        JsonEditor jsonEditor = new JsonEditor(((JsonElement) json).getAsJsonObject());
        jsonEditor.insert(args, value);
        json = jsonEditor.getJson();
    }

    public void set(String path, Object value) {
        if (type == FileType.JSON) {
            setInJson(path, value);
        } else {
            ((FileConfiguration) json).set(path, value);
        }
        if (AutoUpdate) {
            save();
        }
    }

    private static Gson gson = new Gson();

    public void save() {
        if (type == FileType.JSON) {
            String pathInSystem = plugin.getDataFolder() + File.separator + Path;
            try (FileWriter writer = new FileWriter(pathInSystem)) {
                gson.toJson(((JsonElement) json).getAsJsonObject(), writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File f = new File(plugin.getDataFolder(), Path);
            try {
                ((YamlConfiguration) json).save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return plugin.getDataFolder() + File.separator + Path;
    }

    public String getSimpleName() {
        String name = Path;
        if (name.contains(File.separator)) {
            name = name.substring(name.lastIndexOf(File.separator) + 1);
        }
        // remove extension
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        return name;
    }

    public void create() {
        if (extension.toLowerCase().contains("json")) {
            type = FileType.JSON;
        } else if (extension.toLowerCase().contains("yml") || extension.toLowerCase().contains("yaml")) {
            type = FileType.YAML;
        } else {
            type = FileType.UNKNOWN;
        }
        if (type == FileType.JSON) {
            String pathInSystem = plugin.getDataFolder() + File.separator + Path;
            File f = new File(pathInSystem);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                    FileWriter fw = new FileWriter(f);
                    fw.write("{}");
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File f = new File(plugin.getDataFolder(), Path);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Boolean has(String path) {
        if (type == FileType.JSON) {
            JsonEditor jsonEditor = new JsonEditor(((JsonElement) json).getAsJsonObject());
            return jsonEditor.has(path.split("\\."));
        } else {
            return ((FileConfiguration) json).contains(path) && ((FileConfiguration) json).get(path) != null;
        }
    }

    public void has(String path, Runnable runnable) {
        if (has(path)) {
            runnable.run();
        }
    }

    public <T> T getOrSet(String path, T value) {
        if (has(path)) {
            return (T) get(path, value.getClass());
        } else {
            set(path, value);
            return value;
        }
    }

    public <T> T getOrDefault(String path, T value) {
        if (has(path)) {
            return (T) get(path, value.getClass());
        } else {
            return value;
        }
    }

    public void loadDefaults(RealLoader defaults) {
        for (String key : defaults.getDefaultValues().keySet()) {
            if (!has(key)) {
                set(key, defaults.getDefaultValues().get(key));
            }
        }
        save();
    }

    public void loadComments(RealLoader defaults) {
        if (type == FileType.JSON) {
            return;
        }
        File f = new File(plugin.getDataFolder(), Path);
        YamlComentor.addComments(f, defaults.getComments());
    }

    public void loadLoader(RealLoader defaults) {
        loadDefaults(defaults);
        loadComments(defaults);
    }

    public void setComment(Integer line, String comment) {
        if (type == FileType.JSON) {
            return;
        }
        File f = new File(plugin.getDataFolder(), Path);
        YamlComentor.addComment(f, comment, line);
    }

    public Set<String> getKeys() {
        if (type == FileType.JSON) {
            return ((JsonElement) json).getAsJsonObject().keySet();
        } else {
            return ((FileConfiguration) json).getKeys(false);
        }
    }

    public Set<String> getKeys(String path) {
        if (type == FileType.JSON) {
            if (path.contains(".")) {
                String[] args = path.split("\\.");
                JsonEditor jsonEditor = new JsonEditor(((JsonElement) json).getAsJsonObject());
                return jsonEditor.read(args, JsonElement.class).getAsJsonObject().keySet();
            }
        } else {
            return ((FileConfiguration) json).getConfigurationSection(path).getKeys(false);
        }
        return null;
    }

    public InteractiveSection getSection(String path) {
        return new InteractiveSection(this, path);
    }

    public <T> void ifPresent(String path, Class<T> type, Consumer<T> consumer) {
        if (has(path)) {
            consumer.accept(get(path, type));
        }
    }

    public InteractiveFile clone() {
        return new InteractiveFile(Path, plugin);
    }

}
