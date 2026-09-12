package com.mohammadshoubash.taskflow.cache;

import java.util.Optional;

public interface Cache<K, V> {
    Optional<V> get(K key);
    void put(K key, V value);
    void remove(K key);
    boolean contains(K key);
    int size();
    void clear();
}
