package com.automation.engine.modules.conditions.time_based;

import com.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class TimeBasedConditionContext implements IConditionContext {
    private String alias;
    private LocalTime before;
    private LocalTime after;
    private boolean inclusive = false;
}