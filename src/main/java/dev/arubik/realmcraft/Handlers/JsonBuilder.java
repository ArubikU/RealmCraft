package dev.arubik.realmcraft.Handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public final class JsonBuilder {
    private final StringBuilder builder = new StringBuilder();

    public static JsonBuilder create() {
        return new JsonBuilder();
    }

    public JsonBuilder() {
        builder.append("{");
    }

    public JsonBuilder append(String key, String value) {
        builder.append("\"").append(key).append("\":\"").append(value).append("\",");
        return this;
    }

    public JsonBuilder append(String key, int value) {
        builder.append("\"").append(key).append("\":").append(value).append(",");
        return this;
    }

    public JsonBuilder append(String key, boolean value) {
        builder.append("\"").append(key).append("\":").append(value).append(",");
        return this;
    }

    public JsonBuilder append(String key, double value) {
        builder.append("\"").append(key).append("\":").append(value).append(",");
        return this;
    }

    public JsonBuilder append(String key, long value) {
        builder.append("\"").append(key).append("\":").append(value).append(",");
        return this;
    }

    public JsonBuilder append(String key, float value) {
        builder.append("\"").append(key).append("\":").append(value).append(",");
        return this;
    }

    public JsonBuilder append(String key, JsonBuilder value) {
        builder.append("\"").append(key).append("\":").append(value.toString()).append(",");
        return this;
    }

    public JsonBuilder append(String key, JsonArray value) {
        builder.append("\"").append(key).append("\":").append(value.toString()).append(",");
        return this;
    }

    public JsonBuilder append(String key, Object value) {
        builder.append("\"").append(key).append("\":\"").append(value.toString()).append("\",");
        return this;
    }

    public JsonBuilder append(String key, JsonElement value) {
        builder.append("\"").append(key).append("\":").append(value.getAsString()).append(",");
        return this;
    }

    public JsonBuilder append(String key, Object[] value) {
        builder.append("\"").append(key).append("\":[");
        for (Object o : value) {
            builder.append("\"").append(o.toString()).append("\",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("],");
        return this;
    }

    public JsonBuilder append(String key, int[] value) {
        builder.append("\"").append(key).append("\":[");
        for (int o : value) {
            builder.append(o).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("],");
        return this;
    }

    public JsonBuilder append(String key, boolean[] value) {
        builder.append("\"").append(key).append("\":[");
        for (boolean o : value) {
            builder.append(o).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("],");
        return this;
    }

    // generate a Json Element from the StrinBuilder
    public String toString() {
        if (builder.toString().endsWith(",")) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("}");
        return builder.toString();
    }

    public JsonElement toJson() {
        if (builder.toString().endsWith(",")) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("}");
        return new Gson().fromJson(toString(), JsonElement.class);
    }

}