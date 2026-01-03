package com.davidrandoll.automation.engine.test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestLogAppender extends AppenderBase<ILoggingEvent> {
    private final List<String> loggedMessages = new CopyOnWriteArrayList<>();

    @Override
    protected void append(ILoggingEvent event) {
        loggedMessages.add(event.getFormattedMessage());
    }

    public List<String> getLoggedMessages() {
        // Return a snapshot to avoid ConcurrentModificationException during iteration
        return new ArrayList<>(loggedMessages);
    }

    public void clear() {
        loggedMessages.clear();
    }
}