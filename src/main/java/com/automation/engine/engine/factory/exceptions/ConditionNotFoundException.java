package com.automation.engine.engine.factory.exceptions;

public class ConditionNotFoundException extends RuntimeException {
    public ConditionNotFoundException(String name) {
        super("Condition %s not found".formatted(name));
    }
}