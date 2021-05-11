package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class CallableFutureCache<K, V> implements Cache<K, V> {
    private DataSource<K, V> dataSource = null;
    private Map<K, V> cache = new HashMap<>();

    ReentrantLock lock = new ReentrantLock();
    Map<K, FutureTask<V>> futureMap = new HashMap<>();

    public CallableFutureCache(DataSource<K, V> dataSource) {
        this.dataSource = dataSource;
    }

    public V get(long verbose, K key) {
        lock.lock();
        if(verbose > 0) System.out.printf("[%d - %s] lock\n", System.currentTimeMillis(), Thread.currentThread().getName());

        FutureTask<V> futureTask = null;

        if(!cache.containsKey(key)) {
            if(!futureMap.containsKey(key)) {
                futureTask = new FutureTask<>(() -> dataSource.fetch(verbose - 1, key));
                futureMap.put(key, futureTask);
            }

            if(verbose > 0) System.out.printf("[%d - %s] unlock\n", System.currentTimeMillis(), Thread.currentThread().getName());
            lock.unlock();
        } else {
            if(verbose > 0) System.out.printf("[%d - %s] unlock\n", System.currentTimeMillis(), Thread.currentThread().getName());
            lock.unlock();
            return cache.get(key);
        }

        if(futureTask != null) {
            if(verbose > 0) System.out.printf("[%d - %s] fetch start\n", System.currentTimeMillis(), Thread.currentThread().getName());
            futureTask.run();
            if(verbose > 0) System.out.printf("[%d - %s] fetch end\n", System.currentTimeMillis(), Thread.currentThread().getName());
        }

        if(futureMap.containsKey(key)) {
            try {
                FutureTask<V> task = futureMap.get(key);

                if(futureTask != null) {
                    futureMap.remove(key);
                    cache.put(key, task.get());
                }

                return task.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return cache.get(key);
        }
    }
}
