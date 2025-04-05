package com.automation.engine.core;

import com.automation.engine.core.actions.BaseActionList;
import com.automation.engine.core.actions.exceptions.StopAutomationException;
import com.automation.engine.core.conditions.BaseConditionList;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.triggers.BaseTriggerList;
import com.automation.engine.core.variables.BaseVariableList;
import lombok.AllArgsConstructor;
import lombok.Data;

import static java.util.Objects.isNull;

@Data
@AllArgsConstructor
public class Automation {
    private final String alias;
    private final BaseVariableList variables;
    private final BaseTriggerList triggers;
    private final BaseConditionList conditions;
    private final BaseActionList actions;

    /**
     * Set the variables
     */
    public void resolveVariables(EventContext eventContext) {
        if (isNull(variables)) return;
        variables.resolveAll(eventContext);
    }

    /**
     * Check if any of the triggers are triggered
     *
     * @return true if any of the triggers are triggered
     */
    public boolean anyTriggerActivated(EventContext eventContext) {
        if (isNull(triggers)) return false;
        return triggers.anyTriggered(eventContext);
    }

    /**
     * Check if all conditions are met
     *
     * @return true if all conditions are met
     */
    public boolean allConditionsMet(EventContext context) {
        if (isNull(conditions)) return true;
        return conditions.allSatisfied(context);
    }

    /**
     * Perform all actions
     */
    public void performActions(EventContext context) {
        if (isNull(actions)) return;
        try {
            actions.executeAll(context);
        } catch (StopAutomationException e) {
            // This exception is thrown when the automation should be stopped
        }
    }
}