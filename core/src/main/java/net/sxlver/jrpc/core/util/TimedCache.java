package net.sxlver.jrpc.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimedCache<K, V extends TimedCache.NotifyOnExpire> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * how many milliseconds to wait before scheduling expiration.
     * this is important if the timeout value of the {@link V} is
     * being set after it's cached.
     */
    private int expiryReadTimeout = 5;

    public TimedCache() {
    }

    public TimedCache(final int expiryReadTimeout) {
        this.expiryReadTimeout = expiryReadTimeout;
    }

    public void put(K key, V value) {
        cache.put(key, value);

        // wait for timeout value to be set after caching
        scheduler.schedule(() -> expireAfter(key, value.timeout()), expiryReadTimeout, TimeUnit.MILLISECONDS);
    }

    void expireAfter(K key, long millis) {
        scheduler.schedule(() -> {
            final V val = cache.remove(key);
            val.notifyExpired();
        }, millis - expiryReadTimeout, TimeUnit.MILLISECONDS);
    }

    public V get(K key) {
        return cache.get(key);
    }

    public void remove(K key) {
        cache.remove(key);
    }

    public int size() {
        return cache.size();
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    @Override
    public String toString() {
        return cache.toString();
    }

    public interface NotifyOnExpire {
        void notifyExpired();

        long timeout();
    }
}
