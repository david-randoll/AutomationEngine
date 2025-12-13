package com.davidrandoll.automation.engine.core.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NullableConcurrentHashMapTest {

    private NullableConcurrentHashMap<String, String> map;

    @BeforeEach
    void setUp() {
        map = new NullableConcurrentHashMap<>();
    }

    @Test
    void testPutAndGet() {
        // When
        String previous = map.put("key1", "value1");

        // Then
        assertThat(previous).isNull();
        assertThat(map.get("key1")).isEqualTo("value1");
    }

    @Test
    void testPutNull() {
        // When
        map.put("key1", null);

        // Then
        assertThat(map.get("key1")).isNull();
        assertThat(map.containsKey("key1")).isTrue();
    }

    @Test
    void testPutReplace() {
        // Given
        map.put("key1", "value1");

        // When
        String previous = map.put("key1", "value2");

        // Then
        assertThat(previous).isEqualTo("value1");
        assertThat(map.get("key1")).isEqualTo("value2");
    }

    @Test
    void testRemove() {
        // Given
        map.put("key1", "value1");

        // When
        String removed = map.remove("key1");

        // Then
        assertThat(removed).isEqualTo("value1");
        assertThat(map.containsKey("key1")).isFalse();
    }

    @Test
    void testRemoveNonExistent() {
        // When
        String removed = map.remove("nonexistent");

        // Then
        assertThat(removed).isNull();
    }

    @Test
    void testSize() {
        // When
        map.put("key1", "value1");
        map.put("key2", "value2");

        // Then
        assertThat(map.size()).isEqualTo(2);
    }

    @Test
    void testIsEmpty() {
        // Then
        assertThat(map.isEmpty()).isTrue();

        // When
        map.put("key1", "value1");

        // Then
        assertThat(map.isEmpty()).isFalse();
    }

    @Test
    void testClear() {
        // Given
        map.put("key1", "value1");
        map.put("key2", "value2");

        // When
        map.clear();

        // Then
        assertThat(map.isEmpty()).isTrue();
    }

    @Test
    void testContainsKey() {
        // Given
        map.put("key1", "value1");

        // Then
        assertThat(map.containsKey("key1")).isTrue();
        assertThat(map.containsKey("key2")).isFalse();
    }

    @Test
    void testContainsValue() {
        // Given
        map.put("key1", "value1");
        map.put("key2", null);

        // Then
        assertThat(map.containsValue("value1")).isTrue();
        assertThat(map.containsValue(null)).isTrue();
        assertThat(map.containsValue("value2")).isFalse();
    }

    @Test
    void testKeySet() {
        // Given
        map.put("key1", "value1");
        map.put("key2", "value2");

        // When
        var keys = map.keySet();

        // Then
        assertThat(keys).containsExactlyInAnyOrder("key1", "key2");
    }

    @Test
    void testValues() {
        // Given
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", null);

        // When
        var values = map.values();

        // Then
        assertThat(values).containsExactlyInAnyOrder("value1", "value2", null);
    }

    @Test
    void testEntrySet() {
        // Given
        map.put("key1", "value1");
        map.put("key2", null);

        // When
        var entries = map.entrySet();

        // Then
        assertThat(entries).hasSize(2);
        assertThat(entries).anyMatch(e -> "key1".equals(e.getKey()) && "value1".equals(e.getValue()));
        assertThat(entries).anyMatch(e -> "key2".equals(e.getKey()) && e.getValue() == null);
    }

    @Test
    void testPutAll() {
        // Given
        Map<String, String> source = new HashMap<>();
        source.put("key1", "value1");
        source.put("key2", "value2");
        source.put("key3", null);

        // When
        map.putAll(source);

        // Then
        assertThat(map.size()).isEqualTo(3);
        assertThat(map.get("key1")).isEqualTo("value1");
        assertThat(map.get("key2")).isEqualTo("value2");
        assertThat(map.get("key3")).isNull();
        assertThat(map.containsKey("key3")).isTrue();
    }
}
