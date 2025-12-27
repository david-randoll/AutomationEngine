package com.davidrandoll.automation.engine.tracing;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LogEntryTest {

    @Test
    void testBuilder_createsLogEntry() {
        // Arrange
        Instant now = Instant.now();
        Object[] args = new Object[]{"arg1", "arg2"};

        // Act
        LogEntry logEntry = LogEntry.builder()
                .message("Test message with {} and {}")
                .arguments(args)
                .formattedMessage("Test message with arg1 and arg2")
                .timestamp(now)
                .level("INFO")
                .build();

        // Assert
        assertThat(logEntry.getMessage()).isEqualTo("Test message with {} and {}");
        assertThat(logEntry.getArguments()).containsExactly("arg1", "arg2");
        assertThat(logEntry.getFormattedMessage()).isEqualTo("Test message with arg1 and arg2");
        assertThat(logEntry.getTimestamp()).isEqualTo(now);
        assertThat(logEntry.getLevel()).isEqualTo("INFO");
    }

    @Test
    void testNoArgsConstructor() {
        // Act
        LogEntry logEntry = new LogEntry();

        // Assert
        assertThat(logEntry).isNotNull();
        assertThat(logEntry.getMessage()).isNull();
        assertThat(logEntry.getArguments()).isNull();
        assertThat(logEntry.getFormattedMessage()).isNull();
        assertThat(logEntry.getTimestamp()).isNull();
        assertThat(logEntry.getLevel()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        Instant now = Instant.now();
        Object[] args = new Object[]{"test"};

        // Act
        LogEntry logEntry = new LogEntry(
                "message",
                args,
                "formatted message",
                now,
                "DEBUG"
        );

        // Assert
        assertThat(logEntry.getMessage()).isEqualTo("message");
        assertThat(logEntry.getArguments()).containsExactly("test");
        assertThat(logEntry.getFormattedMessage()).isEqualTo("formatted message");
        assertThat(logEntry.getTimestamp()).isEqualTo(now);
        assertThat(logEntry.getLevel()).isEqualTo("DEBUG");
    }

    @Test
    void testSetters() {
        // Arrange
        LogEntry logEntry = new LogEntry();
        Instant now = Instant.now();
        Object[] args = new Object[]{"value"};

        // Act
        logEntry.setMessage("new message");
        logEntry.setArguments(args);
        logEntry.setFormattedMessage("new formatted");
        logEntry.setTimestamp(now);
        logEntry.setLevel("ERROR");

        // Assert
        assertThat(logEntry.getMessage()).isEqualTo("new message");
        assertThat(logEntry.getArguments()).containsExactly("value");
        assertThat(logEntry.getFormattedMessage()).isEqualTo("new formatted");
        assertThat(logEntry.getTimestamp()).isEqualTo(now);
        assertThat(logEntry.getLevel()).isEqualTo("ERROR");
    }

    @Test
    void testEquals_sameValues() {
        // Arrange
        Instant now = Instant.now();
        Object[] args = new Object[]{"test"};
        
        LogEntry entry1 = LogEntry.builder()
                .message("msg")
                .arguments(args)
                .formattedMessage("formatted")
                .timestamp(now)
                .level("INFO")
                .build();

        LogEntry entry2 = LogEntry.builder()
                .message("msg")
                .arguments(args)
                .formattedMessage("formatted")
                .timestamp(now)
                .level("INFO")
                .build();

        // Act & Assert
        assertThat(entry1).isEqualTo(entry2);
    }

    @Test
    void testHashCode() {
        // Arrange
        LogEntry entry1 = LogEntry.builder()
                .message("msg")
                .level("INFO")
                .build();

        LogEntry entry2 = LogEntry.builder()
                .message("msg")
                .level("INFO")
                .build();

        // Act & Assert
        assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        LogEntry entry = LogEntry.builder()
                .message("test message")
                .level("WARN")
                .build();

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains("test message");
        assertThat(result).contains("WARN");
    }
}
