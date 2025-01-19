package com.automation.engine.engine.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ConditionContext {
    private Map<String, Object> data;
}
