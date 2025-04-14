package com.automation.engine.http.extensions;

public class AutomationEngineRequestBodyParseException extends RuntimeException {
    public AutomationEngineRequestBodyParseException(Exception ex) {
        super("Failed to parse request body", ex);
    }
}
