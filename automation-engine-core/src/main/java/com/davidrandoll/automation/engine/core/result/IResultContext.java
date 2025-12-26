package com.davidrandoll.automation.engine.core.result;

import java.util.Map;

public interface IResultContext {
    String getAlias();
    String getDescription();
    Map<String, Object> getOptions();
}
