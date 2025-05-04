package com.davidrandoll.automation.engine.modules.events.time_based;

import com.davidrandoll.automation.engine.core.events.IEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@FieldNameConstants
public class TimeBasedEvent implements IEvent {
    private final LocalTime time;
}