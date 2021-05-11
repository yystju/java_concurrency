package com.example.demo;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class CacheTests {
    @Test
    public void testLockCache() {
        Map<String, String> data = new HashMap<>();

        DataSource<String, String> fakeDataSource = (verbose, key) -> {
            System.out.printf("[%d - %s] fetch key : %s\n", System.currentTimeMillis(), Thread.currentThread().getName(), key);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }

            return data.get(key);
        };

        LockCache<String, String> cache = new LockCache<>(fakeDataSource);

        String key1 = "HELLO";
        String value1 = "WORLD";
        data.put(key1, value1);

        String key2 = "HELLO2";
        String value2 = "WORLD2";
        data.put(key2, value2);

        long verbose = 0;

        int N = 10, T = 5;
        CountDownLatch latch = new CountDownLatch(N * 2);

        Thread key1Thread = new Thread(() -> {
            testSingleKey(verbose - 1, N, T, latch, cache, key1);
        });

        Thread key2Thread = new Thread(() -> {
            testSingleKey(verbose - 1, N, T, latch, cache, key2);
        });

        key1Thread.start();
        key2Thread.start();

        try {
            key1Thread.join();
            key2Thread.join();
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void testIntrinsicCache() {
        Map<String, String> data = new HashMap<>();

        DataSource<String, String> fakeDataSource = (verbose, key) -> {
            System.out.printf("[%d - %s] fetch key : %s\n", System.currentTimeMillis(), Thread.currentThread().getName(), key);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }

            return data.get(key);
        };

        IntrinsicCache<String, String> cache = new IntrinsicCache<>(fakeDataSource);

        String key1 = "HELLO";
        String value1 = "WORLD";
        data.put(key1, value1);

        String key2 = "HELLO2";
        String value2 = "WORLD2";
        data.put(key2, value2);

        long verbose = 10;

        int N = 10, T = 5;
        CountDownLatch latch = new CountDownLatch(N * 2);

        Thread key1Thread = new Thread(() -> {
            testSingleKey(verbose - 1, N, T, latch, cache, key1);
        });

        Thread key2Thread = new Thread(() -> {
            testSingleKey(verbose - 1, N, T, latch, cache, key2);
        });

        key1Thread.start();
        key2Thread.start();

        try {
            key1Thread.join();
            key2Thread.join();
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void testCallableFutureCache() {
        Map<String, String> data = new HashMap<>();

        DataSource<String, String> fakeDataSource = (verbose, key) -> {
            System.out.printf("[%d - %s] fetch key : %s\n", System.currentTimeMillis(), Thread.currentThread().getName(), key);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }

            return data.get(key);
        };

        CallableFutureCache<String, String> cache = new CallableFutureCache<>(fakeDataSource);

        String key1 = "HELLO";
        String value1 = "WORLD";
        data.put(key1, value1);

        String key2 = "HELLO2";
        String value2 = "WORLD2";
        data.put(key2, value2);

        long verbose = 10;

        int N = 2, T = 2;
        CountDownLatch latch = new CountDownLatch(N * 2);

        Thread key1Thread = new Thread(() -> {
            testSingleKey(verbose - 1, N, T, latch, cache, key1);
        });

        Thread key2Thread = new Thread(() -> {
            testSingleKey(verbose - 1, N, T, latch, cache, key2);
        });

        key1Thread.start();
        key2Thread.start();

        try {
            key1Thread.join();
            key2Thread.join();
        } catch (InterruptedException e) {
        }
    }

    private void testSingleKey(long verbose, int N, int T, CountDownLatch latch, Cache<String, String> cache, String key) {
        Runnable r = () -> {
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
            }

            if(verbose > 0) System.out.printf("[%d - %s] started\n", System.currentTimeMillis(), Thread.currentThread().getName());

            for(int i = 0; i < T; ++i) {
                String v = cache.get(verbose - 1, key);
                System.out.printf("[%d - %s] key : %s, value : %s\n", System.currentTimeMillis(), Thread.currentThread().getName(), key, v);
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                }
            }
        };

        Thread[] threads = new Thread[N];

        for(int i = 0; i < N; ++i) {
            threads[i] = new Thread(r);
            threads[i].start();
        }

        for(int i = 0; i < N; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
    }
}
