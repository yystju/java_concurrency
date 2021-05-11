package com.example.demo;

public interface Cache<K, V> {
    V get(long verbose, K key);
}
