package com.davidrandoll.automation.engine.spring.modules.conditions.time_based;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
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
    private LocalTime before;
    private LocalTime after;
    private boolean inclusive = false;
}