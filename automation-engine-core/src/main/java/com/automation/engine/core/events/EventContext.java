package com.automation.engine.core.events;

import com.automation.engine.core.AutomationEngine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;

/**
 * EventContext is a class that represents the context of an event.
 * It contains the event data and metadata associated with the event.
 */
@Slf4j
@Getter
public class EventContext {
    private final IEvent event;
    /**
     * Additional metadata associated with the event.
     * for example, any variables or additional information that needs to be passed along with the event.
     */
    private final Map<String, Object> metadata;
    private final Class<? extends IEvent> type;
    private final Instant timestamp;
    private final String source;

    public EventContext(IEvent event) {
        if (isNull(event)) throw new IllegalArgumentException("Event cannot be null");

        this.event = event;
        this.metadata = new ConcurrentHashMap<>(); // some actions may be executed in parallel
        this.type = event.getClass();
        this.timestamp = Instant.now();
        this.source = getFromStackTrace(this.getClass());
    }

    public static EventContext of(IEvent event) {
        return new EventContext(event);
    }

    /**
     * This method retrieves the name of the event.
     * It uses the class name of the event as the event name.
     *
     * @return the event object
     */
    public String getEventName() {
        return this.getType().getSimpleName();
    }

    /**
     * This method retrieves the event data.
     * It builds a map from the event data and adds metadata to it.
     * The event data is obtained using reflection to get all fields of the event class and their values.
     * <p>
     * NOTE: the metadata overrides the event data if there are any conflicts.
     * </p>
     *
     * @return a map containing the event data and metadata
     */
    public Map<String, Object> getEventData() {
        var result = new HashMap<String, Object>();
        result.putAll(buildMapFromEventData());
        result.putAll(metadata);
        return result;
    }

    /**
     * This method adds metadata to the event context.
     * A metadata map can contain any additional information that needs to be passed along with the event.
     *
     * @param metadata a map containing any variables or other information that is not part of the event data.
     */
    public void addMetadata(Map<String, Object> metadata) {
        if (isNull(metadata)) return;
        this.metadata.putAll(metadata);
    }

    /**
     * This method adds metadata to the event context.
     * A metadata map can contain any additional information that needs to be passed along with the event.
     *
     * @param key   the key for the metadata entry
     * @param value the value for the metadata entry
     */
    public void addMetadata(String key, Object value) {
        if (isNull(key)) throw new IllegalArgumentException("Key cannot be null");
        var map = new HashMap<String, Object>();
        map.put(key, value);
        addMetadata(map);
    }

    /**
     * This method removes metadata from the event context.
     * It can be used to remove any metadata that is no longer needed.
     *
     * @param key the key for the metadata entry to be removed
     */
    public void removeMetadata(String key) {
        if (isNull(key)) throw new IllegalArgumentException("Key cannot be null");
        metadata.remove(key);
    }

    /**
     * This method retrieves metadata from the event context.
     * It can be used to access any metadata that has been added to the event context.
     *
     * @param key the key for the metadata entry to be retrieved
     * @return the value associated with the specified key, or null if no such key exists
     */
    public Object getMetadata(String key) {
        if (isNull(key)) throw new IllegalArgumentException("Key cannot be null");
        Map<String, Object> eventData = getEventData();
        if (isNull(eventData)) return null;
        return eventData.get(key);
    }

    /**
     * This method builds a map from the event data.
     * It uses reflection to get all fields of the event class and their values.
     * The field names are used as keys in the map.
     *
     * @return a map containing the event data
     */
    private Map<String, Object> buildMapFromEventData() {
        var result = new HashMap<String, Object>();
        var fields = FieldUtils.getAllFields(event.getClass());
        for (var field : fields) {
            try {
                result.put(field.getName(), FieldUtils.readField(field, event, true));
            } catch (IllegalAccessException e) {
                log.error("Error reading field {} from event {}", field.getName(), event.getClass().getSimpleName(), e);
            }
        }
        return result;
    }

    /**
     * Get the class name of the caller from the stack trace.
     *
     * @return the class name of the caller
     */
    public static String getFromStackTrace(Class<?> clazz) {
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
        for (var i = 0; i < stackTraces.length; i++) {
            var foundAutomationEngine = AutomationEngine.class.getName().equals(stackTraces[i].getClassName());
            if (foundAutomationEngine) {
                // check the class before if it is the same as clazz
                // if yes return the class at i+1
                // return the class at i-1
                if (i < 1 || i + 1 >= stackTraces.length) break;
                var foundCallingClass = clazz.getName().equals(stackTraces[i - 1].getClassName());
                if (foundCallingClass) {
                    return stackTraces[i + 1].getClassName();
                } else {
                    return stackTraces[i].getClassName();
                }
            }
        }

        return "";
    }
}