package com.davidrandoll.automation.engine.core.events.publisher;

import com.davidrandoll.automation.engine.core.Automation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutomationEngineRemoveEvent {
    private Automation automation;
}