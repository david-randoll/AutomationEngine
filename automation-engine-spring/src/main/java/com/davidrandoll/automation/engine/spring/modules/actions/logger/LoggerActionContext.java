package com.davidrandoll.automation.engine.spring.modules.actions.logger;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.slf4j.event.Level;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        LoggerActionContext.Fields.alias,
        LoggerActionContext.Fields.description,
        LoggerActionContext.Fields.level,
        LoggerActionContext.Fields.message
})
public class LoggerActionContext implements IActionContext {
    /** Unique identifier for this action */
    private String alias;

    /** Human-readable description of what this action does */
    private String description;

    /**
     * Log level for the message (TRACE, DEBUG, INFO, WARN, ERROR). Defaults to INFO
     */
    @ContextField(
        helpText = "Select log level: TRACE (most verbose), DEBUG, INFO, WARN, or ERROR (least verbose)"
    )
    private Level level = Level.INFO;

    /** Message to log. Supports template expressions for dynamic content */
    @ContextField(
        widget = ContextField.Widget.TEXTAREA,
        placeholder = "Enter log message (supports template expressions like {{ event.data }})",
        helpText = "Message to log. Supports Pebble template expressions for dynamic content."
    )
    private Object message = "No message";
}