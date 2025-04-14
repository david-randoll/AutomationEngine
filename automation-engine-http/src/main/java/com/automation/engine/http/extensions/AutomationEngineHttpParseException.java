package com.automation.engine.http.extensions;

public class AutomationEngineHttpParseException extends RuntimeException {
    public AutomationEngineHttpParseException(Exception ex) {
        super("Failed to parse request body", ex);
    }
}
