package com.davidrandoll.automation.engine.tracing;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TracingAppenderTest {

    @Mock
    private ILoggingEvent loggingEvent;

    private TracingAppender appender;
    private TraceContext traceContext;

    @BeforeEach
    void setUp() {
        appender = new TracingAppender();
        appender.start();
        traceContext = new TraceContext("test-automation");
        TraceContext.setThreadContext(traceContext);
    }

    @Test
    void testAppend_capturesLogEntry() {
        // Arrange
        when(loggingEvent.getMessage()).thenReturn("Test message with {}");
        when(loggingEvent.getArgumentArray()).thenReturn(new Object[] { "arg1" });
        when(loggingEvent.getFormattedMessage()).thenReturn("Test message with arg1");
        when(loggingEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(loggingEvent.getLevel()).thenReturn(Level.INFO);

        traceContext.startLogCapture();

        // Act
        appender.append(loggingEvent);

        // Assert
        var logs = traceContext.stopLogCapture();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getMessage()).isEqualTo("Test message with {}");
        assertThat(logs.get(0).getArguments()).containsExactly("arg1");
        assertThat(logs.get(0).getFormattedMessage()).isEqualTo("Test message with arg1");
        assertThat(logs.get(0).getLevel()).isEqualTo("INFO");
        assertThat(logs.get(0).getTimestamp()).isNotNull();
    }

    @Test
    void testAppend_capturesErrorLevel() {
        // Arrange
        when(loggingEvent.getMessage()).thenReturn("Error occurred");
        when(loggingEvent.getArgumentArray()).thenReturn(new Object[] {});
        when(loggingEvent.getFormattedMessage()).thenReturn("Error occurred");
        when(loggingEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(loggingEvent.getLevel()).thenReturn(Level.ERROR);

        traceContext.startLogCapture();

        // Act
        appender.append(loggingEvent);

        // Assert
        var logs = traceContext.stopLogCapture();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo("ERROR");
    }

    @Test
    void testAppend_capturesWarnLevel() {
        // Arrange
        when(loggingEvent.getMessage()).thenReturn("Warning message");
        when(loggingEvent.getArgumentArray()).thenReturn(new Object[] {});
        when(loggingEvent.getFormattedMessage()).thenReturn("Warning message");
        when(loggingEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(loggingEvent.getLevel()).thenReturn(Level.WARN);

        traceContext.startLogCapture();

        // Act
        appender.append(loggingEvent);

        // Assert
        var logs = traceContext.stopLogCapture();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo("WARN");
    }

    @Test
    void testAppend_capturesDebugLevel() {
        // Arrange
        when(loggingEvent.getMessage()).thenReturn("Debug info");
        when(loggingEvent.getArgumentArray()).thenReturn(new Object[] {});
        when(loggingEvent.getFormattedMessage()).thenReturn("Debug info");
        when(loggingEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(loggingEvent.getLevel()).thenReturn(Level.DEBUG);

        traceContext.startLogCapture();

        // Act
        appender.append(loggingEvent);

        // Assert
        var logs = traceContext.stopLogCapture();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo("DEBUG");
    }

    @Test
    void testAppend_capturesTraceLevel() {
        // Arrange
        when(loggingEvent.getMessage()).thenReturn("Trace details");
        when(loggingEvent.getArgumentArray()).thenReturn(new Object[] {});
        when(loggingEvent.getFormattedMessage()).thenReturn("Trace details");
        when(loggingEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(loggingEvent.getLevel()).thenReturn(Level.TRACE);

        traceContext.startLogCapture();

        // Act
        appender.append(loggingEvent);

        // Assert
        var logs = traceContext.stopLogCapture();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo("TRACE");
    }

    @Test
    void testAppend_handlesNullArguments() {
        // Arrange
        when(loggingEvent.getMessage()).thenReturn("Message");
        when(loggingEvent.getArgumentArray()).thenReturn(null);
        when(loggingEvent.getFormattedMessage()).thenReturn("Message");
        when(loggingEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(loggingEvent.getLevel()).thenReturn(Level.INFO);

        traceContext.startLogCapture();

        // Act
        appender.append(loggingEvent);

        // Assert
        var logs = traceContext.stopLogCapture();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getArguments()).isNull();
    }

    @Test
    void testAppend_capturesMultipleLogs() {
        // Arrange
        when(loggingEvent.getMessage()).thenReturn("Message 1", "Message 2", "Message 3");
        when(loggingEvent.getArgumentArray()).thenReturn(new Object[] {});
        when(loggingEvent.getFormattedMessage()).thenReturn("Message 1", "Message 2", "Message 3");
        when(loggingEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(loggingEvent.getLevel()).thenReturn(Level.INFO);

        traceContext.startLogCapture();

        // Act
        appender.append(loggingEvent);
        appender.append(loggingEvent);
        appender.append(loggingEvent);

        // Assert
        var logs = traceContext.stopLogCapture();
        assertThat(logs).hasSize(3);
    }

    @Test
    void testAppend_doesNothingWhenNoTraceContext() {
        // Arrange
        TraceContext.clearThreadContext();
        when(loggingEvent.getMessage()).thenReturn("Message");
        when(loggingEvent.getArgumentArray()).thenReturn(new Object[] {});
        when(loggingEvent.getFormattedMessage()).thenReturn("Message");
        when(loggingEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(loggingEvent.getLevel()).thenReturn(Level.INFO);

        // Act - should not throw exception
        appender.append(loggingEvent);

        // Assert - no exception means success
    }
}
