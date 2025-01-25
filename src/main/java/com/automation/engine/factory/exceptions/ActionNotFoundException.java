package com.automation.engine.factory.exceptions;

public class ActionNotFoundException extends RuntimeException {
    public ActionNotFoundException(String name) {
        super("Action %s not found".formatted(name));
    }
}