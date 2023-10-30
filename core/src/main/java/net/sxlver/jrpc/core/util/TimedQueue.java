package net.sxlver.jrpc.core.util;


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class TimedQueue<K, V extends TimedQueue.NotifyOnExpire> {
    private final Queue<Entry<K, V>> queue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * how many milliseconds to wait before scheduling expiration.
     * this is important if the timeout value of the {@link V} is
     * being set after it's enqueued.
     */
    private int expiryReadTimeout = 5;

    public TimedQueue() {
    }

    public TimedQueue(final int expiryReadTimeout) {
        this.expiryReadTimeout = expiryReadTimeout;
    }

    public void enqueue(K key, V value) {
        Entry<K, V> entry = new Entry<>(key, value);
        queue.add(entry);

        // wait for timeout value to be set after enqueuing
        scheduler.schedule(() -> expireAfter(entry), expiryReadTimeout, TimeUnit.MILLISECONDS);
    }

    void expireAfter(Entry<K, V> entry) {
        scheduler.schedule(() -> {
            if (queue.remove(entry)) {
                entry.value.notifyExpired();
            }
        }, entry.value.timeout() - expiryReadTimeout, TimeUnit.MILLISECONDS);
    }

    public V dequeue() {
        Entry<K, V> entry = queue.poll();
        return entry != null ? entry.value : null;
    }

    public int size() {
        return queue.size();
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    @Override
    public String toString() {
        return queue.toString();
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        while(!queue.isEmpty()) {
            final Entry<K, V> entry = queue.poll();
            action.accept(entry.key, entry.value);
        }
    }

    public interface NotifyOnExpire {
        void notifyExpired();

        long timeout();
    }

    private static class Entry<K, V> {
        private final K key;
        private final V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
