package com.automation.engine.engine.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class EventContext {
    private Map<String, Object> data;
}