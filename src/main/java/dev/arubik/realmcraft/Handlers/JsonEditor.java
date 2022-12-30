package dev.arubik.realmcraft.Handlers;

import java.lang.reflect.Array;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.val;

public class JsonEditor {

    @Getter
    private JsonObject json;

    public JsonEditor(JsonObject json) {
        this.json = json;
    }

    public void insert(String[] path, Object data) {
        JsonElement value = new Gson().toJsonTree(data);
        JsonObject last = json;
        final int pathLenght = path.length - 2;
        for (int i = 0; i <= pathLenght; i++) {
            final String pathElement = path[i].replace("<dot>", "\\.");
            JsonElement next = last.get(pathElement);
            if (next == null) {
                JsonObject newObject = new JsonObject();
                last.add(pathElement, newObject);
                last = newObject;
                continue;
            }

            if (!(next instanceof JsonObject)) {
                // que debo hacer aqui chat gpt?
            }

            last = (JsonObject) next;
        }
        final String key = path[pathLenght + 1].replace("<dot>", "\\.");
        JsonElement lastElement = last.get(key);
        if (lastElement == null) {
            last.add(key, new Gson().toJsonTree(""));
            lastElement = last.get(key);
        }
        if (lastElement instanceof JsonArray) {
            ((JsonArray) lastElement).add(value);
        } else {
            last.add(key, value);
        }
    }

    public <T> T read(String[] path, Class<T> type) {
        JsonElement last = json;
        for (String pathElement : path) {
            pathElement = pathElement.replace("<dot>", "\\.");
            if (last instanceof JsonObject) {
                last = ((JsonObject) last).get(pathElement);
            } else if (last instanceof JsonArray && !(type == List.class || type == Array.class)) {
                last = ((JsonArray) last).get(Integer.parseInt(pathElement));
            }
            if (last == null) {
                return null;
            }
        }
        if (type == List.class || type == Array.class) {
            return (T) Lists.newArrayList(last.getAsJsonArray());
        }
        return new Gson().fromJson(last, type);
    }

    public Boolean has(String[] path) {
        JsonElement last = json;
        for (String pathElement : path) {
            pathElement = pathElement.replace("<dot>", "\\.");
            if (last instanceof JsonObject) {
                last = ((JsonObject) last).get(pathElement);
            } else if (last instanceof JsonArray) {
                last = ((JsonArray) last).get(Integer.parseInt(pathElement));
            }
            if (last == null) {
                return false;
            }
        }
        return true;
    }
}