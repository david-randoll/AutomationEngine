package com.automation.engine.creator.conditions;

public class ConditionNotFoundException extends RuntimeException {
    public ConditionNotFoundException(String name) {
        super("Condition %s not found".formatted(name));
    }
}