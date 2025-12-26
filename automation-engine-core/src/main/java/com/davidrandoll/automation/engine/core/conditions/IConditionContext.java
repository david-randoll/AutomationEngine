package com.davidrandoll.automation.engine.core.conditions;

import java.util.HashMap;
import java.util.Map;

public interface IConditionContext {
    String getAlias();

    String getDescription();

    default Map<String, Object> getOptions() {
        return new HashMap<>();
    }
}
