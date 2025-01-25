package com.automation.engine.modules.time_based;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeBasedTriggerContext {
    private LocalTime beforeTime;
    private LocalTime afterTime;
}