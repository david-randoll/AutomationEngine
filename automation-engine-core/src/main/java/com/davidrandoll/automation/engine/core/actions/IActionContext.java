package com.davidrandoll.automation.engine.core.actions;

import java.util.HashMap;
import java.util.Map;

public interface IActionContext {
    String getAlias();

    String getDescription();

    default Map<String, Object> getOptions() {
        return new HashMap<>();
    }
}