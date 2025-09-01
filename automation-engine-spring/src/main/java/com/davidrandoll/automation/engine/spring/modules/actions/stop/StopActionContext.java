package com.davidrandoll.automation.engine.spring.modules.actions.stop;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
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
    private ConditionDefinition condition;
    private boolean stopActionSequence;
    private boolean stopAutomation;
    private String stopMessage;

    public boolean hasStopMessage() {
        return !ObjectUtils.isEmpty(stopMessage);
    }
}