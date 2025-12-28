package com.davidrandoll.automation.engine.spring.modules.actions.stop;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.util.ObjectUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        StopActionContext.Fields.alias,
        StopActionContext.Fields.description,
        StopActionContext.Fields.condition,
        StopActionContext.Fields.stopActionSequence,
        StopActionContext.Fields.stopAutomation,
        StopActionContext.Fields.stopMessage
})
public class StopActionContext implements IActionContext {
    private String alias;
    private String description;

    @ContextField(
        helpText = "Optional condition to evaluate. If present and true, execution stops"
    )
    private ConditionDefinition condition;

    @ContextField(
        widget = ContextField.Widget.SWITCH,
        helpText = "Stop executing remaining actions in the current sequence"
    )
    private boolean stopActionSequence;

    @ContextField(
        widget = ContextField.Widget.SWITCH,
        helpText = "Stop the entire automation execution"
    )
    private boolean stopAutomation;

    @ContextField(
        widget = ContextField.Widget.TEXTAREA,
        placeholder = "Stopped because...",
        helpText = "Message to include in the stop event. Supports template expressions"
    )
    private String stopMessage;

    public boolean hasStopMessage() {
        return !ObjectUtils.isEmpty(stopMessage);
    }
}