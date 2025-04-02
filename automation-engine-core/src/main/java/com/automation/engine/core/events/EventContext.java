package com.automation.engine.core.events;

import lombok.Getter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class EventContext {
    @Getter
    private final IEvent event;
    private final Map<String, Object> metadata;

    public EventContext(IEvent event) {
        this.event = event;
        this.metadata = new HashMap<>();
    }

    public static EventContext of(IEvent event) {
        return new EventContext(event);
    }

    public String getName() {
        return event.getClass().getSimpleName();
    }

    @NonNull
    public Map<String, Object> getEventData() {
        var result = new HashMap<String, Object>();
        result.putAll(getFieldValue());
        result.putAll(metadata);
        return result;
    }

    @NonNull
    private Map<String, Object> getFieldValue() {
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


    public void addVariables(@Nullable Map<String, Object> variables) {
        if (ObjectUtils.isEmpty(variables)) return;
        metadata.putAll(variables);
    }

    public void addVariable(@NonNull String key, @Nullable Object value) {
        var map = new HashMap<String, Object>();
        map.put(key, value);
        addVariables(map);
    }

    public void removeVariable(@NonNull String key) {
        metadata.remove(key);
    }

    @Nullable
    public Object getVariable(@NonNull String key) {
        Map<String, Object> eventData = getEventData();
        if (ObjectUtils.isEmpty(eventData)) return null;
        return eventData.get(key);
    }
}