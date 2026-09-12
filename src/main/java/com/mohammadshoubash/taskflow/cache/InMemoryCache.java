package com.mohammadshoubash.taskflow.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, V> cache = new HashMap<>();

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public boolean contains(K key) {
        return cache.containsKey(key);
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
