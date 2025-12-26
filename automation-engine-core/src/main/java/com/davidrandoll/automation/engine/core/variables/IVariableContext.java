package com.davidrandoll.automation.engine.core.variables;

import java.util.Map;

public interface IVariableContext {
    String getAlias();
    String getDescription();
    Map<String, Object> getOptions();
}
