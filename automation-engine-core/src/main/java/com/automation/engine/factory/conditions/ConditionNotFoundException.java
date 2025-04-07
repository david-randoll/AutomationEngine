package com.automation.engine.factory.conditions;

public class ConditionNotFoundException extends RuntimeException {
    public ConditionNotFoundException(String name) {
        super("Condition %s not found".formatted(name));
    }
}