package dev.arubik.realmcraft.FileManagement;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import static com.google.gson.JsonParser.*;

import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.JsonEditor;
import dev.arubik.realmcraft.Handlers.JsonEditor.JsonPath;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
                    FileReader fileReader = new FileReader(path);
                    json = parseString(fileReader.toString());
                    type = FileType.JSON;
                    fileReader.close();
                } catch (JsonSyntaxException | IOException e) {
                    json = JsonBuilder.create().toJson();
                }
            }
            case "yml":
            case "yaml":
            case ".yaml":
            case ".yml": {
                File f = new File(plugin.getDataFolder(), path);
                if (!f.exists()) {
                    f.getParentFile().mkdirs();
                    YamlConfiguration s = YamlConfiguration.loadConfiguration(f);
                    org.bukkit.configuration.file.FileConfiguration data = (org.bukkit.configuration.file.FileConfiguration) s;
                    data.options().copyDefaults(true);
                }
                YamlConfiguration s = YamlConfiguration.loadConfiguration(f);
                json = s;
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

    // generate all the setters and getters of info like getInteger, getBoolean,
    // getString, etc
    public Integer getInteger(String path) {
        if (type == FileType.JSON) {
            return Integer.parseInt(get(path).toString());
        } else {
            return ((FileConfiguration) json).getInt(path);
        }
    }

    public Integer getInteger(String path, Integer def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return Integer.parseInt(get(path).toString());
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
            return get(path).toString();
        } else {
            return ((FileConfiguration) json).getString(path);
        }
    }

    public String getString(String path, String def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return get(path).toString();
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
            return Boolean.parseBoolean(get(path).toString());
        } else {
            return ((FileConfiguration) json).getBoolean(path);
        }
    }

    public Boolean getBoolean(String path, Boolean def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return Boolean.parseBoolean(get(path).toString());
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
            return Double.parseDouble(get(path).toString());
        } else {
            return ((FileConfiguration) json).getDouble(path);
        }
    }

    public Double getDouble(String path, Double def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return Double.parseDouble(get(path).toString());
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
            return Long.parseLong(get(path).toString());
        } else {
            return ((FileConfiguration) json).getLong(path);
        }
    }

    public Long getLong(String path, Long def) {
        if (type == FileType.JSON) {
            if (get(path) != null) {
                return Long.parseLong(get(path).toString());
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
        JsonObject obj = ((JsonElement) json).getAsJsonObject();
        JsonPath jsonPath = JsonEditor.parsePath(path);
        json = JsonEditor.setValue(jsonPath, value, obj);
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

    public void save() {
        if (type == FileType.JSON) {
            String pathInSystem = plugin.getDataFolder() + File.separator + Path;
            try (FileWriter writer = new FileWriter(pathInSystem)) {
                PrintWriter printerWriter = new PrintWriter(writer);
                printerWriter.write(json.toString());
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
        return name;
    }
}
