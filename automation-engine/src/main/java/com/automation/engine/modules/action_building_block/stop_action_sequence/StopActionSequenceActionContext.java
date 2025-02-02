package com.automation.engine.modules.action_building_block.stop_action_sequence;

import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.factory.request.Condition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopActionSequenceActionContext implements IActionContext {
    private String alias;
    private boolean stopIfSatisfied = true;
    private Condition condition;
}