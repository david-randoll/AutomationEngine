package com.davidrandoll.automation.engine.core.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ConditionContext {
    private Map<String, Object> data;
}
