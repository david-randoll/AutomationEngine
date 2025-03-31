package com.automation.engine.modules.action_building_block.wait_for_trigger;

import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.factory.model.Trigger;
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
    private List<Trigger> triggers;
    private Duration timeout;
}