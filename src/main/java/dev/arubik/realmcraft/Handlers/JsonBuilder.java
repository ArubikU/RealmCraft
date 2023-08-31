package dev.arubik.realmcraft.Handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import dev.arubik.realmcraft.Api.RealNBT.AllowedTypes;
import dev.arubik.realmcraft.Api.RealNBT.NBTTag;

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
        if (value == null)
            value = "null";

        // verify if value is array
        if (value.getClass().isArray()) {
            return append(key, (Object[]) value);
        }
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

    public JsonBuilder append(NBTTag tag) {

        if (tag.getValue() == null) {
            return this;
        }

        switch (tag.getType()) {
            case String:
                return append(tag.getKey(), tag.getValue().toString());
            case Integer:
                return append(tag.getKey(), (int) tag.getValue());
            case Double:
                return append(tag.getKey(), (double) tag.getValue());
            case Float:
                return append(tag.getKey(), (float) tag.getValue());
            case Long:
                return append(tag.getKey(), (long) tag.getValue());
            case Boolean:
                return append(tag.getKey(), (boolean) tag.getValue());
            case Byte:
                return append(tag.getKey(), (byte) tag.getValue());
            case Short:
                return append(tag.getKey(), (short) tag.getValue());
            case NBTTag:
                return append(tag.getKey(), ((NBTTag) tag.getValue()).toJson());
            case OBJECT:
                return append(tag.getKey(), tag.getValue());
            case JsonElement:
                return append(tag.getKey(), tag.getValue());
            default:
                break;
        }
        return this;
    }

    // generate a Json Element from the StrinBuilder
    public String toString() {
        StringBuilder builderclone = new StringBuilder(builder.toString());
        if (builderclone.toString().endsWith(",")) {
            builderclone.deleteCharAt(builderclone.length() - 1);
        }
        builderclone.append("}");
        return builderclone.toString();
    }

    public JsonElement toJson() {
        return new Gson().fromJson(toString(), JsonElement.class);
    }

}