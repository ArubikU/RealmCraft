package dev.arubik.realmcraft.FileManagement;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
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

import dev.arubik.realmcraft.DefaultConfigs.RealLoader;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.JsonEditor;
import dev.arubik.realmcraft.Handlers.RealMessage;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class InteractiveFile {

    public enum FileType {
        JSON, YAML, UNKNOWN
    }

    private static Plugin plugin = null;

    private String Path;
    @Setter
    private boolean AutoUpdate;
    private Object json;
    private FileType type = FileType.UNKNOWN;
    private String name;

    public FileType getType() {
        return type;
    }

    public InteractiveFile(String path) {
        String extension = path.substring(path.lastIndexOf("."));
        Path = path;
        name = path.substring(path.lastIndexOf("/") + 1);
        // verifiy if the plugin have a file with the same path in resources
        // if not, create a new file with the same path in resources
        if (!(new File(plugin.getDataFolder(), path).exists())) {
            if (plugin.getResource(path) != null) {
                plugin.saveResource(path, false);
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

    public static void setPlugin(Plugin Pl) {
        plugin = Pl;
    }

    public Object getFile() {
        return json;
    }

    // generate a main getter if the file is a json
    private Object get(String path) {
        if (type == FileType.JSON) {
            JsonElement a = ((JsonElement) json);
            for (String s : path.split("\\.")) {
                a = a.getAsJsonObject().get(s);
            }
            return a;
        }
        return null;
    }

    private <T> T get(String path, Class<T> itype) {
        if (type == FileType.JSON) {
            JsonEditor je = new JsonEditor((JsonObject) json);
            return (T) je.read(path.split("\\."), itype);
        }
        return null;
    }

    // generate all the setters and getters of info like getInteger, getBoolean,
    // getString, etc
    public Integer getInteger(String path) {
        if (type == FileType.JSON) {
            return get(path, Integer.class);
        } else {
            return ((FileConfiguration) json).getInt(path);
        }
    }

    public Integer getInteger(String path, Integer def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return get(path, Integer.class);
            }
            return def;
        } else {
            return ((FileConfiguration) json).getInt(path, def);
        }
    }

    public Integer getInteger(String[] path) {
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
        if (type == FileType.JSON) {
            return get(path, String.class);
        } else {
            return ((FileConfiguration) json).getString(path);
        }
    }

    public String getString(String path, String def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return get(path, String.class);
            }
            return def;
        } else {
            return ((FileConfiguration) json).getString(path, def);
        }
    }

    public String getString(String[] path) {
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
        if (type == FileType.JSON) {
            return Boolean.parseBoolean(getString(path));
        } else {
            return ((FileConfiguration) json).getBoolean(path);
        }
    }

    public Boolean getBoolean(String path, Boolean def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return Boolean.parseBoolean(getString(path));
            }
            return def;
        } else {
            return ((FileConfiguration) json).getBoolean(path, def);
        }
    }

    public Boolean getBoolean(String[] path) {
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

    public Double getDouble(String[] path) {
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

    public Long getLong(String[] path) {
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

    public List<String> getStringList(String[] path) {
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

    public List<Integer> getIntegerList(String[] path) {
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

    public void create() {
        if (type == FileType.JSON) {
            String pathInSystem = plugin.getDataFolder() + File.separator + Path;
            File f = new File(pathInSystem);
            if (!f.exists()) {
                try {
                    f.createNewFile();
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
            return ((FileConfiguration) json).contains(path);
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

    public void loadDefaults(RealLoader defaults) {
        for (String key : defaults.getDefaultValues().keySet()) {
            if (!has(key)) {
                set(key, defaults.getDefaultValues().get(key));
            }
        }
        save();
    }
}
