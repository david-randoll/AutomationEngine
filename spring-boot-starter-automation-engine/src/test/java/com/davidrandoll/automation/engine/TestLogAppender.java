package com.davidrandoll.automation.engine;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class TestLogAppender extends AppenderBase<ILoggingEvent> {
    private final List<String> loggedMessages = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent event) {
        loggedMessages.add(event.getFormattedMessage());
    }

    public List<String> getLoggedMessages() {
        return loggedMessages;
    }
}