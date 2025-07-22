package com.davidrandoll.automation.engine.spring.web.exceptions;

public class AutomationEngineInvalidRegexException extends RuntimeException {
    public AutomationEngineInvalidRegexException(String regex, Exception ex) {
        super("Invalid regex pattern: %s".formatted(regex), ex);
    }
}