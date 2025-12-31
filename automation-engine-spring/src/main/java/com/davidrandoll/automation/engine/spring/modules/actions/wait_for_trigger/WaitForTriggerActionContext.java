package com.davidrandoll.automation.engine.spring.modules.actions.wait_for_trigger;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class WaitForTriggerActionContext implements IActionContext {
    private String alias;
    private String description;
    private Duration timeout;
    private List<TriggerDefinition> triggers;
}
