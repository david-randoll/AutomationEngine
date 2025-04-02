package com.automation.engine.core.events;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor
public abstract class Event {
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> metadata;

    public String getName() {
        return getClass().getSimpleName();
    }

    @NonNull
    public Map<String, Object> getEventData() {
        var result = new HashMap<String, Object>();
        result.putAll(getFieldValue());
        result.putAll(Optional.ofNullable(metadata).orElse(Map.of()));
        return result;
    }

    @NonNull
    public abstract Map<String, Object> getFieldValue();


    public void addVariables(@Nullable Map<String, Object> variables) {
        if (ObjectUtils.isEmpty(variables)) return;
        if (ObjectUtils.isEmpty(metadata)) metadata = new HashMap<>();
        metadata = new HashMap<>(metadata);
        metadata.putAll(variables);
    }

    public void addVariable(@NonNull String key, @Nullable Object value) {
        var map = new HashMap<String, Object>();
        map.put(key, value);
        addVariables(map);
    }

    public void removeVariable(@NonNull String key) {
        if (ObjectUtils.isEmpty(metadata)) return;
        metadata = new HashMap<>(metadata);
        metadata.remove(key);
    }

    @Nullable
    public Object getVariable(@NonNull String key) {
        if (ObjectUtils.isEmpty(getEventData())) return null;
        return getEventData().get(key);
    }
}