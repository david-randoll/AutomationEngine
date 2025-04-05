package com.automation.engine.core.events;

import com.automation.engine.core.AutomationEngine;
import lombok.Getter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventContext is a class that represents the context of an event.
 * It contains the event data and metadata associated with the event.
 */
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
        Assert.notNull(event, "Event cannot be null");
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
    @NonNull
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
    public void addMetadata(@Nullable Map<String, Object> metadata) {
        if (ObjectUtils.isEmpty(metadata)) return;
        this.metadata.putAll(metadata);
    }

    /**
     * This method adds metadata to the event context.
     * A metadata map can contain any additional information that needs to be passed along with the event.
     *
     * @param key   the key for the metadata entry
     * @param value the value for the metadata entry
     */
    public void addMetadata(@NonNull String key, @Nullable Object value) {
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
    public void removeMetadata(@NonNull String key) {
        metadata.remove(key);
    }

    /**
     * This method retrieves metadata from the event context.
     * It can be used to access any metadata that has been added to the event context.
     *
     * @param key the key for the metadata entry to be retrieved
     * @return the value associated with the specified key, or null if no such key exists
     */
    @Nullable
    public Object getMetadata(@NonNull String key) {
        Map<String, Object> eventData = getEventData();
        if (ObjectUtils.isEmpty(eventData)) return null;
        return eventData.get(key);
    }

    /**
     * This method builds a map from the event data.
     * It uses reflection to get all fields of the event class and their values.
     * The field names are used as keys in the map.
     *
     * @return a map containing the event data
     */
    @NonNull
    private Map<String, Object> buildMapFromEventData() {
        var result = new HashMap<String, Object>();
        var fields = FieldUtils.getAllFields(event.getClass());
        for (var field : fields) {
            try {
                result.put(field.getName(), FieldUtils.readField(field, event, true));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
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