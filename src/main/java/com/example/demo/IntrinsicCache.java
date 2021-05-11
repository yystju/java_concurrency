package com.example.demo;

import java.util.HashMap;
import java.util.Map;

public class IntrinsicCache<K, V> implements Cache<K, V> {
    private DataSource<K, V> dataSource = null;
    private Map<K, V> cache = new HashMap<>();

    Object lock = new Object();
    Map<K, Object> keyLockMap = new HashMap<>();

    public IntrinsicCache(DataSource<K, V> dataSource) {
        this.dataSource = dataSource;
    }

    public V get(long verbose, K key) {
        synchronized (lock) {
            if (verbose > 0)
                System.out.printf("[%d - %s] lock\n", System.currentTimeMillis(), Thread.currentThread().getName());

            if(cache.containsKey(key)) {
                if(verbose > 0)
                    System.out.printf("[%d - %s] unlock\n", System.currentTimeMillis(), Thread.currentThread().getName());

                return cache.get(key);
            } else {
                if(!keyLockMap.containsKey(key)) {
                    keyLockMap.put(key, new Object());
                }
            }

            if(verbose > 0) System.out.printf("[%d - %s] unlock\n", System.currentTimeMillis(), Thread.currentThread().getName());
        }

        synchronized (keyLockMap.get(key)) {
            if(verbose > 0) System.out.printf("[%d - %s] keylock.lock\n", System.currentTimeMillis(), Thread.currentThread().getName());

            if(cache.containsKey(key)) {
                return cache.get(key);
            } else {
                if(verbose > 0) System.out.printf("[%d - %s] fetch start\n", System.currentTimeMillis(), Thread.currentThread().getName());
                V v = dataSource.fetch(verbose - 1, key);
                cache.put(key, v);
                if(verbose > 0) System.out.printf("[%d - %s] fetch end\n", System.currentTimeMillis(), Thread.currentThread().getName());
                keyLockMap.remove(key);

                if(verbose > 0) System.out.printf("[%d - %s] keylock.unlock\n", System.currentTimeMillis(), Thread.currentThread().getName());
                return v;
            }
        }
    }
}
