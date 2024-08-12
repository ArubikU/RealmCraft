package dev.arubik.realmcraft.FileManagement;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import dev.arubik.realmcraft.Handlers.RealMessage;
import lombok.Getter;
import lombok.val;

public class InteractiveSection {
    @Getter
    private InteractiveFile file;
    @Getter
    private String path;

    public InteractiveSection(InteractiveFile file, String path) {
        this.file = file;
        this.path = path;
    }

    public InteractiveSection getSection(String path) {
        return new InteractiveSection(file, this.path + "." + path);
    }

    public List<InteractiveSection> getSections(String path) {
        Set<String> keys = file.getKeys(this.path + "." + path);
        val sections = new InteractiveSection[keys.size()];
        int i = 0;
        for (String key : keys) {
            sections[i] = getSection(path + "." + key);
            i++;
        }
        return List.of(sections);

    }

    public List<InteractiveSection> getSections() {
        Set<String> keys = file.getKeys(this.path);
        val sections = new InteractiveSection[keys.size()];
        int i = 0;
        for (String key : keys) {
            sections[i] = getSection(key);
            i++;
        }
        return List.of(sections);
    }

    public void set(String path, Object value) {
        file.set(this.path + "." + path, value);
    }

    public Boolean has(String path) {
        return file.has(this.path + "." + path);
    }

    public void has(String path, Runnable runnable) {
        if (has(path)) {
            runnable.run();
        }
    }

    public Set<String> getKeys() {
        return file.getKeys(this.path);
    }

    public List<String> getStringList(String path) {
        return file.getStringList(this.path + "." + path);
    }

    public List<String> getStringList(String path, List<String> value) {
        if (!has(path)) {
            return value;
        } else {
            return getStringList(path);
        }
    }

    public @Nullable Object get(@NotNull String path) {
        return file.get(this.path + "." + path);
    }

    public <T> T get(@NotNull String path, @NotNull Class<T> itype) {
        return file.get(this.path + "." + path, itype);
    }

    public <T> T getOrSet(@NotNull String path, @NotNull T value) {
        return file.getOrSet(this.path + "." + path, value);
    }

    public <T> T getOrDefault(@NotNull String path, @NotNull T value) {
        return file.get(this.path + "." + path) == null ? value : (T) file.get(this.path + "." + path);
    }

    public InteractiveSection clone() {
        return new InteractiveSection(file.clone(), path);
    }
}