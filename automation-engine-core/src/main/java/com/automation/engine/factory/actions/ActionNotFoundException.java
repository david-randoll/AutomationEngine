package com.automation.engine.factory.actions;

public class ActionNotFoundException extends RuntimeException {
    public ActionNotFoundException(String name) {
        super("Action %s not found".formatted(name));
    }
}