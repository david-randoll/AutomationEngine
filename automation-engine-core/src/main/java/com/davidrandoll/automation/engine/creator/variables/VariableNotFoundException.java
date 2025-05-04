package com.davidrandoll.automation.engine.creator.variables;

public class VariableNotFoundException extends RuntimeException {
    public VariableNotFoundException(String name) {
        super("Variable %s not found".formatted(name));
    }
}