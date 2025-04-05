package com.automation.engine.core.events;

import com.automation.engine.core.Automation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutomationEngineRemoveEvent {
    private Automation automation;
}