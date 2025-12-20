package com.davidrandoll.automation.engine.spring.tracing;

import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Helper utility for collecting and managing trace data in EventContext metadata.
 * Thread-safe operations for appending trace entries and capturing context snapshots.
 */
@Slf4j
public final class TraceDataCollector {
    
    private TraceDataCollector() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Append a trace entry to a list in EventContext metadata.
     * Creates the list if it doesn't exist. Thread-safe.
     *
     * @param eventContext The event context
     * @param listKey The metadata key for the trace list
     * @param entry The trace entry to append
     */
    @SuppressWarnings("unchecked")
    public static void appendToTraceList(EventContext eventContext, String listKey, Map<String, Object> entry) {
        if (eventContext == null || listKey == null || entry == null) {
            return;
        }
        
        synchronized (eventContext.getMetadata()) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) eventContext.getMetadata().get(listKey);
            if (list == null) {
                list = new ArrayList<>();
                eventContext.getMetadata().put(listKey, list);
            }
            list.add(new HashMap<>(entry)); // Defensive copy
        }
    }
    
    /**
     * Capture a snapshot of the EventContext metadata.
     * Optionally includes only keys or full key-value pairs.
     *
     * @param eventContext The event context
     * @param keysOnly If true, only capture metadata keys; if false, capture full key-value pairs
     * @return A snapshot map
     */
    public static Map<String, Object> captureContextSnapshot(EventContext eventContext, boolean keysOnly) {
        if (eventContext == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> snapshot = new HashMap<>();
        
        synchronized (eventContext.getMetadata()) {
            if (keysOnly) {
                snapshot.put("metadataKeys", new ArrayList<>(eventContext.getMetadata().keySet()));
            } else {
                // Deep copy to avoid references
                eventContext.getMetadata().forEach((key, value) -> {
                    if (!key.startsWith("__trace_")) { // Exclude trace data from snapshots
                        snapshot.put(key, serializeValue(value));
                    }
                });
            }
        }
        
        snapshot.put("eventType", eventContext.getType().getName());
        snapshot.put("timestamp", eventContext.getTimestamp().toString());
        snapshot.put("source", eventContext.getSource());
        
        return snapshot;
    }
    
    /**
     * Safely serialize a value for trace storage.
     * Handles null, primitives, strings, collections, and complex objects.
     *
     * @param value The value to serialize
     * @return A serializable representation
     */
    private static Object serializeValue(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        
        if (value instanceof Collection) {
            return ((Collection<?>) value).size() + " items";
        }
        
        if (value instanceof Map) {
            return ((Map<?, ?>) value).size() + " entries";
        }
        
        // For complex objects, use toString
        try {
            return value.getClass().getSimpleName() + "@" + Integer.toHexString(value.hashCode());
        } catch (Exception e) {
            return "Object";
        }
    }
    
    /**
     * Get a trace list from EventContext metadata.
     *
     * @param eventContext The event context
     * @param listKey The metadata key for the trace list
     * @return The trace list, or empty list if not found
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getTraceList(EventContext eventContext, String listKey) {
        if (eventContext == null || listKey == null) {
            return Collections.emptyList();
        }
        
        Object value = eventContext.getMetadata().get(listKey);
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Check if tracing is enabled for the given EventContext.
     * Tracing is enabled if the TRACE_ID metadata key is present.
     *
     * @param eventContext The event context
     * @return true if tracing is enabled
     */
    public static boolean isTracingEnabled(EventContext eventContext) {
        return eventContext != null && eventContext.getMetadata().containsKey(TraceConstants.TRACE_ID);
    }
    
    /**
     * Create a trace entry map with common timing fields.
     *
     * @param startNanos Start time in nanoseconds
     * @param endNanos End time in nanoseconds
     * @return A map with timing fields
     */
    public static Map<String, Object> createTimingEntry(long startNanos, long endNanos) {
        Map<String, Object> entry = new HashMap<>();
        entry.put(TraceConstants.FIELD_START_TIME_NANOS, startNanos);
        entry.put(TraceConstants.FIELD_END_TIME_NANOS, endNanos);
        entry.put(TraceConstants.FIELD_DURATION_NANOS, endNanos - startNanos);
        return entry;
    }
}
