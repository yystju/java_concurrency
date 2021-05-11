package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class LockCache<K, V> implements Cache<K, V> {
    private DataSource<K, V> dataSource = null;
    private Map<K, V> cache = new HashMap<>();

    ReentrantLock lock = new ReentrantLock();
    Map<K, ReentrantLock> keyLockMap = new HashMap<>();

    public LockCache(DataSource<K, V> dataSource) {
        this.dataSource = dataSource;
    }

    public V get(long verbose, K key) {
        lock.lock();
        if(verbose > 0) System.out.printf("[%d - %s] lock\n", System.currentTimeMillis(), Thread.currentThread().getName());

        if(verbose > 0) System.out.printf("[%d - %s] keyLockMap.containsKey(key) : %s\n", System.currentTimeMillis(), Thread.currentThread().getName(), keyLockMap.containsKey(key));

        if(keyLockMap.containsKey(key)) {
            keyLockMap.get(key).lock();
            if(verbose > 0) System.out.printf("[%d - %s] keylock.lock\n", System.currentTimeMillis(), Thread.currentThread().getName());
        }

        if(cache.containsKey(key)) {
            ReentrantLock keyLock = keyLockMap.get(key);
            if(keyLock != null && keyLock.isLocked()) {
                keyLock.unlock();
            }
            if(verbose > 0) System.out.printf("[%d - %s] unlock\n", System.currentTimeMillis(), Thread.currentThread().getName());
            lock.unlock();
            return cache.get(key);
        } else {
            if(!keyLockMap.containsKey(key)) {
                keyLockMap.put(key, new ReentrantLock());
            }
            keyLockMap.get(key).lock();
            if(verbose > 0) System.out.printf("[%d - %s] keylock.lock\n", System.currentTimeMillis(), Thread.currentThread().getName());
            if(verbose > 0) System.out.printf("[%d - %s] unlock\n", System.currentTimeMillis(), Thread.currentThread().getName());
            lock.unlock();
            if(verbose > 0) System.out.printf("[%d - %s] fetch start\n", System.currentTimeMillis(), Thread.currentThread().getName());
            V v = dataSource.fetch(verbose - 1, key);
            cache.put(key, v);
            if(verbose > 0) System.out.printf("[%d - %s] fetch end\n", System.currentTimeMillis(), Thread.currentThread().getName());
            keyLockMap.get(key).unlock();
            keyLockMap.remove(key);
            if(verbose > 0) System.out.printf("[%d - %s] keylock.unlock\n", System.currentTimeMillis(), Thread.currentThread().getName());
            return v;
        }
    }
}
