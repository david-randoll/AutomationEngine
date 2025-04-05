package com.automation.engine.core.events.publisher;

import com.automation.engine.core.Automation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutomationEngineRegisterEvent {
    private Automation automation;
}