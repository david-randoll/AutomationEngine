package com.davidrandoll.automation.engine.core.triggers;

import java.util.Map;

public interface ITriggerContext {
    String getAlias();
    String getDescription();
    Map<String, Object> getOptions();
}
