package com.automation.engine.modules.time_based.condition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class TimeBasedConditionContext {
    private LocalTime before;
    private LocalTime after;
}