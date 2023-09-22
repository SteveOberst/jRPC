package net.sxlver.jrpc.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimedCache<K, V> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final long timeout;
    private final TimeUnit timeUnit;

    public TimedCache(final long timeout, final TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public void put(K key, V value, long timeoutMs) {
        cache.put(key, value);

        scheduler.schedule(() -> {
            final V val = cache.remove(key);
            if(val instanceof NotifyOnExpire receiver)
                receiver.notifyExpired();
        }, timeoutMs, TimeUnit.MILLISECONDS);
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

    public interface NotifyOnExpire {
        void notifyExpired();
    }
}
