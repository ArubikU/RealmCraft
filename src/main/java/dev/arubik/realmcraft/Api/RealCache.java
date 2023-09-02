package dev.arubik.realmcraft.Api;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import dev.arubik.realmcraft.IReplacer.InternalReplacerStructure;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class RealCache<T> {
    private T value;
    private boolean isCached = false;
    private long lastUpdate = 0;
    private long updateInterval = 0;
    private long removeInterval = 0;

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public RealCache() {
        this.value = null;
        this.isCached = false;
    }

    public RealCache(long removeInterval) {
        this.value = null;
        this.isCached = false;
        this.removeInterval = removeInterval;
    }

    public RealCache(T value) {
        this.isCached = true;
        set(value);
    }

    public RealCache(T value, long updateInterval) {
        this.isCached = true;
        this.updateInterval = updateInterval;
        set(value);
    }

    public RealCache(T value, long updateInterval, long removeInterval) {
        this.isCached = true;
        this.updateInterval = updateInterval;
        this.removeInterval = removeInterval;
        set(value);
    }

    @Nullable
    public T get() throws Exception {
        if (isCached) {
            if (updateInterval != 0) {
                if (System.currentTimeMillis() - lastUpdate > updateInterval) {
                    isCached = false;
                    throw new Exception("Cache expired");
                }
            }
            if (removeInterval != 0) {
                if (System.currentTimeMillis() - lastUpdate > removeInterval) {
                    isCached = false;
                    value = null;
                }
            }
        } else {
            throw new Exception("Cache not set");
        }

        this.lastUpdate = System.currentTimeMillis();
        timer();
        return value;
    }

    @Nullable
    public T get(T def) {
        try {
            return get();
        } catch (Exception e) {
            return def;
        }
    }

    public Optional<T> getOptional() {
        try {
            return Optional.of(get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Nullable
    public T forcedGet() {
        try {
            return get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void set(T value) {
        this.value = value;
        this.isCached = true;
        this.lastUpdate = System.currentTimeMillis();
        timer();
    }

    public void cache(T value) {
        this.value = value;
        this.isCached = true;
        this.lastUpdate = System.currentTimeMillis();
        timer();
    }

    public void timer() {

        if (removeInterval != 0) {
            Runnable task = () -> {
                remove();
            };
            executor.schedule(task, removeInterval, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }

    public void remove() {
        if (System.currentTimeMillis() - lastUpdate > removeInterval) {
            this.value = null;
            this.isCached = false;
        }
    }

    public boolean isCached() {
        return isCached;
    }

    public void refresh() {
        this.lastUpdate = System.currentTimeMillis();
    }

    public static class RealCacheMap<K, V> {
        private HashMap<K, RealCache<V>> map = new HashMap<>();
        @Setter
        private long updateInterval = 0;
        @Setter
        private long removeInterval = 0;

        public RealCacheMap() {

        }

        public RealCacheMap(long removeInterval) {
            this.removeInterval = removeInterval;
        }

        public RealCacheMap(long updateInterval, long removeInterval) {
            this.updateInterval = updateInterval;
            this.removeInterval = removeInterval;
        }

        public void clear() {
            map.clear();
        }

        public void set(K key, V value) {
            map.put(key, new RealCache<V>(value, updateInterval, removeInterval));
        }

        public void set(K key, V value, long updateInterval) {
            map.put(key, new RealCache<V>(value, updateInterval, removeInterval));
        }

        public void set(K key, V value, long updateInterval, long removeInterval) {
            map.put(key, new RealCache<V>(value, updateInterval, removeInterval));
        }

        @Nullable
        public V get(K key) {
            try {
                return map.get(key).get();
            } catch (Exception e) {
                return null;
            }
        }

        @Nullable
        public RealCache<V> getCache(K key) {
            return map.get(key);
        }

        public Optional<V> getOptional(K key) {
            try {
                return Optional.of(get(key));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        @Nullable
        public V get(K key, V def) {
            return map.get(key).get(def);
        }

        @Nullable
        public V getFunc(K key) {
            if (containsKey(key)) {
                return get(key);
            } else {
                return function.apply(key);
            }
        }

        public Function<K, V> function = null;

        public void setFunction(Function<K, V> function) {
            this.function = function;
        }

        public Set<K> keySet() {
            return map.keySet();
        }

        public boolean containsKey(K key) {
            Boolean contains = map.containsKey(key);
            if (contains) {
                if (!map.get(key).isCached()) {
                    map.remove(key);
                    return false;
                }
                map.get(key).refresh();
            }
            return contains;
        }

        public void put(K bytes2, V structure) {
            set(bytes2, structure);
        }

        public void remove(K key) {
            map.get(key).remove();
            map.remove(key);
        }
    }
}
