package com.automation.engine.core.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@AllArgsConstructor
public class Event {
    @Getter
    private String name;
    private Map<String, Object> data;

    @NonNull
    public Map<String, Object> getData() {
        return Optional.ofNullable(data).orElse(Map.of());
    }

    public void addVariables(@Nullable Map<String, Object> variables) {
        if (ObjectUtils.isEmpty(variables)) return;
        if (ObjectUtils.isEmpty(data)) data = new HashMap<>();
        data = new HashMap<>(data);
        data.putAll(variables);
    }

    public void addVariable(@NonNull String key, @Nullable Object value) {
        var map = new HashMap<String, Object>();
        map.put(key, value);
        addVariables(map);
    }

    public void removeVariable(@NonNull String key) {
        if (ObjectUtils.isEmpty(data)) return;
        data = new HashMap<>(data);
        data.remove(key);
    }

    @Nullable
    public Object getVariable(@NonNull String key) {
        if (ObjectUtils.isEmpty(getData())) return null;
        return getData().get(key);
    }
}