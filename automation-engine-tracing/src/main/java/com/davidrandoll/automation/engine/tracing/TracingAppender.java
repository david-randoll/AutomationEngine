package com.davidrandoll.automation.engine.tracing;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Logback appender that routes log messages to the current TraceContext.
 * When tracing is enabled, this appender captures all SLF4J log messages
 * and associates them with the currently executing automation component.
 */
public class TracingAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent event) {
        TraceContext.recordLog(event.getFormattedMessage());
    }
}
