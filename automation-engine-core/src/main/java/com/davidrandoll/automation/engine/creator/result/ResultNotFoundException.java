package com.davidrandoll.automation.engine.creator.result;

public class ResultNotFoundException extends RuntimeException {
    public ResultNotFoundException(String name) {
        super("Result %s not found".formatted(name));
    }
}