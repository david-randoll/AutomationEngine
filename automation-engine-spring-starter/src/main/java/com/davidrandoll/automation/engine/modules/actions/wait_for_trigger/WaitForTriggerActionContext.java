package com.davidrandoll.automation.engine.modules.actions.wait_for_trigger;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.triggers.Trigger;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaitForTriggerActionContext implements IActionContext {
    private String alias;
    private String description;
    private List<Trigger> triggers;
    private Duration timeout;
}