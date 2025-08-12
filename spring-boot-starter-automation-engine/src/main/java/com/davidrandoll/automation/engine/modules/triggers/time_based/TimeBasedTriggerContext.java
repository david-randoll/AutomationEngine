package com.davidrandoll.automation.engine.modules.triggers.time_based;

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
    private String alias;
    private String description;
    private LocalTime at;
}