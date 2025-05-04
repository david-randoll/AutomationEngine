package com.davidrandoll.automation.engine.core.actions.exceptions;

public class StopActionSequenceException extends RuntimeException {
    public StopActionSequenceException() {
        super("Stop action sequence");
    }

    public StopActionSequenceException(String message) {
        super(message);
    }
}