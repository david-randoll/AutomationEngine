package com.automation.engine.core.events.publisher;

import com.automation.engine.core.Automation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AutomationEngineRemoveAllEvent {
    private List<Automation> automations;
}