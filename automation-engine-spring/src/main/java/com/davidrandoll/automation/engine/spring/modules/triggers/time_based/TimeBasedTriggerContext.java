package com.davidrandoll.automation.engine.spring.modules.triggers.time_based;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
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
        TimeBasedTriggerContext.Fields.at
})
public class TimeBasedTriggerContext implements ITriggerContext {
    /** Unique identifier for this trigger */
    private String alias;

    /** Human-readable description of what this trigger responds to */
    private String description;

    /** Time of day when this trigger should activate (format: HH:mm:ss or HH:mm) */
    private LocalTime at;
}