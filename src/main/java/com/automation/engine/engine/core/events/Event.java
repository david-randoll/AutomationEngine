package com.automation.engine.engine.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Event {
    private String name;
    private Map<String, Object> data;

    public EventContext getContext() {
        return new EventContext(data);
    }
}
