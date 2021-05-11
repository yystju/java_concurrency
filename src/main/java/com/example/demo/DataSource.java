package com.example.demo;

public interface DataSource<K, V> {
    V fetch(long verbose, K key);
}
