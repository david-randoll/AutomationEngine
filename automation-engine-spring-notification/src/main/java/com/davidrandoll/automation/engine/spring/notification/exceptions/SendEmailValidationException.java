package com.davidrandoll.automation.engine.spring.notification.exceptions;

public class SendEmailValidationException extends RuntimeException {
    public SendEmailValidationException(String message) {
        super(message);
    }
}
