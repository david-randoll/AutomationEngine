package com.davidrandoll.automation.engine.modules.actions.stop;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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