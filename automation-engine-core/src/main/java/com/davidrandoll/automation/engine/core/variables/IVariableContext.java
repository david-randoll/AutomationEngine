package com.davidrandoll.automation.engine.core.variables;

import java.util.HashMap;
import java.util.Map;

public interface IVariableContext {
    String getAlias();
    String getDescription();
    default Map<String, Object> getOptions() {
        return new HashMap<>();
    }
}