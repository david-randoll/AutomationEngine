package com.davidrandoll.automation.engine.modules.triggers.time_based;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeBasedTriggerContext implements ITriggerContext {
    private String alias;
    private String description;
    private LocalTime at;
}