package com.davidrandoll.automation.engine.core.triggers;

import java.util.Map;

public interface ITriggerContext {
    String getAlias();
    String getDescription();
    default Map<String, Object> getOptions() {
        return Map.of();
    }
}
