package com.davidrandoll.automation.engine.spring.modules.actions.wait_for_trigger;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
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

    @ContextField(
        helpText = "List of triggers to wait for. Execution pauses until any trigger activates"
    )
    private List<TriggerDefinition> triggers;

    @ContextField(
        placeholder = "PT30S",
        helpText = "Maximum time to wait (ISO-8601 format: PT30S = 30 seconds, PT5M = 5 minutes)"
    )
    private Duration timeout;
}