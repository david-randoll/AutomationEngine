package com.automation.engine.factory.variables;

public class VariableNotFoundException extends RuntimeException {
    public VariableNotFoundException(String name) {
        super("Variable %s not found".formatted(name));
    }
}