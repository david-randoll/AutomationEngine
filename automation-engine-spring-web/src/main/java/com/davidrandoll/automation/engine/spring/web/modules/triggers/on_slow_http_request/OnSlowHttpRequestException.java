package com.davidrandoll.automation.engine.spring.web.modules.triggers.on_slow_http_request;

public class OnSlowHttpRequestException extends RuntimeException {
    public OnSlowHttpRequestException(String message) {
        super(message);
    }
}
