package com.davidrandoll.automation.engine.core.conditions;

import java.util.Map;

public interface IConditionContext {
    String getAlias();
    String getDescription();
    Map<String, Object> getOptions();
}
