package com.automation.engine.factory.exceptions;

public class TriggerNotFoundException extends RuntimeException {
    public TriggerNotFoundException(String name) {
        super("Trigger %s not found".formatted(name));
    }
}