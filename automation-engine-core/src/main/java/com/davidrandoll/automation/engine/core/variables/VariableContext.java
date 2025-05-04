package com.davidrandoll.automation.engine.core.variables;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class VariableContext {
    private Map<String, Object> data;
}