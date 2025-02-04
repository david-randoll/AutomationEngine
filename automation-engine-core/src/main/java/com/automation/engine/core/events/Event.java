package com.automation.engine.core.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

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
}