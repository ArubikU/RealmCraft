package dev.arubik.realmcraft.Api;

import java.util.HashMap;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

    public T get(T def) {
        try {
            return get();
        } catch (Exception e) {
            return def;
        }
    }

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

    public static class RealCacheMap<K, V> {
        private HashMap<K, RealCache<V>> map = new HashMap<>();
        @Setter
        private long updateInterval = 0;
        @Setter
        private long removeInterval = 0;

        public RealCacheMap() {

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

        public V get(K key) {
            return map.get(key).forcedGet();
        }

        public Set<K> keySet() {
            return map.keySet();
        }

        public boolean containsKey(K key) {
            return map.containsKey(key);
        }

        public void put(K bytes2, V structure) {
            set(bytes2, structure);
        }

    }
}
