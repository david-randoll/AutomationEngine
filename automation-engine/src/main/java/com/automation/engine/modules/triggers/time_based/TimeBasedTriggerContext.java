package com.automation.engine.modules.triggers.time_based;

import com.automation.engine.core.triggers.ITriggerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeBasedTriggerContext implements ITriggerContext {
    private String alias;
    private LocalTime at;
}