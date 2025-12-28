package com.davidrandoll.automation.engine.spring.modules.conditions.time_based;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
@FieldNameConstants
@JsonPropertyOrder({
        TimeBasedConditionContext.Fields.alias,
        TimeBasedConditionContext.Fields.description,
        TimeBasedConditionContext.Fields.before,
        TimeBasedConditionContext.Fields.after,
        TimeBasedConditionContext.Fields.inclusive
})
public class TimeBasedConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @ContextField(
        widget = ContextField.Widget.TIME,
        placeholder = "17:00",
        helpText = "Condition is true if current time is before this time (24-hour format)"
    )
    private LocalTime before;

    @ContextField(
        widget = ContextField.Widget.TIME,
        placeholder = "09:00",
        helpText = "Condition is true if current time is after this time (24-hour format)"
    )
    private LocalTime after;

    @ContextField(
        widget = ContextField.Widget.SWITCH,
        helpText = "If true, boundary times are included in the comparison"
    )
    private boolean inclusive = false;
}