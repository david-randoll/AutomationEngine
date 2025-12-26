package com.davidrandoll.automation.engine.core.actions;

import java.util.Map;

public interface IActionContext {
    String getAlias();
    String getDescription();
    default Map<String, Object> getOptions() {
        return Map.of();
    }
}