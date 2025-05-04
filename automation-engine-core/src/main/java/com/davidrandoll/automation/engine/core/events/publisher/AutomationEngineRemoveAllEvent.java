package com.davidrandoll.automation.engine.core.events.publisher;

import com.davidrandoll.automation.engine.core.Automation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AutomationEngineRemoveAllEvent {
    private List<Automation> automations;
}