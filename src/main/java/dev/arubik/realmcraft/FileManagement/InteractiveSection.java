package dev.arubik.realmcraft.FileManagement;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

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

    public void set(String path, Object value) {
        file.set(this.path + "." + path, value);
    }

    @Nullable
    public <T> T get(String path, Class<T> clazz) {
        return file.get(this.path + "." + path, clazz);
    }

    public <T> T getOrSet(String path, T value) {
        return file.getOrSet(this.path + "." + path, value);
    }

    public <T> T getOrDefault(String path, T value) {
        return file.getOrDefault(this.path + "." + path, value);
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

    public Object get(String path) {
        return file.get(this.path + "." + path);
    }
}