package com.automation.engine.modules.time_based.condition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeBasedConditionContext {
    private LocalTime before;
    private LocalTime after;
}