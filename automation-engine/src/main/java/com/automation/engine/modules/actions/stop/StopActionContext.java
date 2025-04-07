package com.automation.engine.modules.actions.stop;

import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.factory.conditions.Condition;
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
    private Condition condition;
    private boolean stopActionSequence;
    private boolean stopAutomation;
    private String stopMessage;

    public boolean hasStopMessage() {
        return !ObjectUtils.isEmpty(stopMessage);
    }
}