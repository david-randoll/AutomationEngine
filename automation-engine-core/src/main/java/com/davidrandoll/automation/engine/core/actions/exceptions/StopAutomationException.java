package com.davidrandoll.automation.engine.core.actions.exceptions;

public class StopAutomationException extends RuntimeException {
    public StopAutomationException() {
        super("Stop automation");
    }

    public StopAutomationException(String message) {
        super(message);
    }
}