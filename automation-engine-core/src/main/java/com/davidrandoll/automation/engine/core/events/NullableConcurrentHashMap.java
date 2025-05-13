package com.davidrandoll.automation.engine.core.events;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NullableConcurrentHashMap<K, V> implements Map<K, V> {
    private final Map<K, Optional<V>> map = new ConcurrentHashMap<>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        // compound operation: synchronized for consistency
        synchronized (map) {
            for (Optional<V> optional : map.values()) {
                if (Objects.equals(optional.orElse(null), value)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public V get(Object key) {
        Optional<V> optional = map.get(key);
        return optional == null ? null : optional.orElse(null);
    }

    @Override
    public V put(K key, V value) {
        Optional<V> previous = map.put(key, Optional.ofNullable(value));
        return previous == null ? null : previous.orElse(null);
    }

    @Override
    public V remove(Object key) {
        Optional<V> removed = map.remove(key);
        return removed == null ? null : removed.orElse(null);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        synchronized (map) {
            for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                map.put(entry.getKey(), Optional.ofNullable(entry.getValue()));
            }
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        synchronized (map) {
            return map.values().stream()
                    .map(opt -> opt.orElse(null))
                    .toList();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        synchronized (map) {
            return map.entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().orElse(null)))
                    .collect(Collectors.toSet());
        }
    }
}