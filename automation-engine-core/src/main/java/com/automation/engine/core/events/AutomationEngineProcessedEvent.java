package com.automation.engine.core.events;

import com.automation.engine.core.Automation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutomationEngineProcessedEvent {
    private Automation automation;
    private EventContext eventContext;
}