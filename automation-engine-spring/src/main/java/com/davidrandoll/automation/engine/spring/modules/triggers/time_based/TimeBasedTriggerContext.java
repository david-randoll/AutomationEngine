package com.davidrandoll.automation.engine.spring.modules.triggers.time_based;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        TimeBasedTriggerContext.Fields.alias,
        TimeBasedTriggerContext.Fields.description,
        TimeBasedTriggerContext.Fields.at,
        TimeBasedTriggerContext.Fields.cron
})
public class TimeBasedTriggerContext implements ITriggerContext {
    /** Unique identifier for this trigger */
    private String alias;

    /** Human-readable description of what this trigger responds to */
    private String description;

    /** Time of day when this trigger should activate (format: HH:mm:ss or HH:mm) */
    @ContextField(widget = ContextField.Widget.TIME, placeholder = "09:00", helpText = "Time of day when this trigger activates (24-hour format: HH:mm or HH:mm:ss)")
    private LocalTime at;

    /** Cron expression for recurring activation */
    @ContextField(placeholder = "0 0 9 * * *", helpText = "Cron expression for recurring activation (e.g., '0 0 9 * * *' for every day at 9 AM)")
    private String cron;
}