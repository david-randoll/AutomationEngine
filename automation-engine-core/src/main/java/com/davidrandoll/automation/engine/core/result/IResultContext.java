package com.davidrandoll.automation.engine.core.result;

import java.util.HashMap;
import java.util.Map;

public interface IResultContext {
    String getAlias();
    String getDescription();
    default Map<String, Object> getOptions() {
        return new HashMap<>();
    }
}
