package dev.arubik.realmcraft.Handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class JsonEditor {
    public static class JsonPath {
        private String key;
        private JsonPath parent;

        public JsonPath(String key, JsonPath parent) {
            this.key = key;
            this.parent = parent;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public JsonPath getParent() {
            return parent;
        }

        public void setParent(JsonPath parent) {
            this.parent = parent;
        }
    }

    public static JsonPath parsePath(String path) {
        String[] keys = path.split("\\.");
        JsonPath current = null;
        for (int i = keys.length - 1; i >= 0; i--) {
            current = new JsonPath(keys[i], current);
        }
        return current;
    }

    public static Object followPath(JsonPath path, JsonObject json) {
        if (path.getParent() != null) {
            json = (JsonObject) followPath(path.getParent(), json);
        }
        return json.get(path.getKey());
    }

    public static JsonObject setValue(JsonPath path, Object value, JsonObject json) {
        if (path.getParent() != null) {
            json = (JsonObject) followPath(path.getParent(), json);
        }
        json.add(path.getKey(), new Gson().toJsonTree(value));
        return json;
    }

    public static Object getValue(String path, JsonObject json) {
        return followPath(parsePath(path), json);
    }
}