package com.example.demo;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Queue<T> {
    private int size;
    private long checkInterval;

    private LinkedList<T> data = new LinkedList<>();
    private Lock lock = new ReentrantLock();

    private Condition conditionEmpty = lock.newCondition();
    private Condition conditionFull = lock.newCondition();

    public Queue(int size) {
        this(size, 100L);
    }

    public Queue(int size, long checkInterval) {
        this.size = size;
        this.checkInterval = checkInterval;
    }

    public void push(T t) throws InterruptedException {
        try {
            lock.lock();

            while(data.size() >= size) {
                conditionFull.await(checkInterval, TimeUnit.MILLISECONDS);
            }

            data.push(t);

            conditionEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T pop() throws InterruptedException {
        T ret = null;

        try {
            lock.lock();

            while(data.size() == 0) {
                conditionEmpty.await(checkInterval, TimeUnit.MILLISECONDS);
            }

            ret = data.pop();

            conditionFull.signal();
        } finally {
            lock.unlock();
        }

        return ret;
    }
}
