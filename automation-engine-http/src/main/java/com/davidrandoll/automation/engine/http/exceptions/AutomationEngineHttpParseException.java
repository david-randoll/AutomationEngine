package com.davidrandoll.automation.engine.http.exceptions;

public class AutomationEngineHttpParseException extends RuntimeException {
    public AutomationEngineHttpParseException(Exception ex) {
        super("Failed to parse request body", ex);
    }
}
