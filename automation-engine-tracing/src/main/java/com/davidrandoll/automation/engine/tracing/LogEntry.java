package com.davidrandoll.automation.engine.tracing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a captured log entry with structured information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    /**
     * The log message template (before argument substitution)
     */
    private String message;

    /**
     * Arguments passed to the logger
     */
    private Object[] arguments;

    /**
     * The fully formatted log message
     */
    private String formattedMessage;

    /**
     * Timestamp when the log was created
     */
    private Instant timestamp;
}
