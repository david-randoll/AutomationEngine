package com.davidrandoll.automation.engine.modules.actions.wait_for_trigger;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Duration;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        WaitForTriggerActionContext.Fields.alias,
        WaitForTriggerActionContext.Fields.description,
        WaitForTriggerActionContext.Fields.triggers,
        WaitForTriggerActionContext.Fields.timeout
})
public class WaitForTriggerActionContext implements IActionContext {
    private String alias;
    private String description;
    private List<TriggerDefinition> triggers;
    private Duration timeout;
}