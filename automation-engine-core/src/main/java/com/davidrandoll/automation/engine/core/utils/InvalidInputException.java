package com.davidrandoll.automation.engine.core.utils;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(Exception e) {
        super(e);
    }
}