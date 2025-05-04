package com.davidrandoll.automation.engine.core.events.publisher;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutomationEngineProcessedEvent {
    private Automation automation;
    private EventContext eventContext;
}