package com.automation.engine.core;

import com.automation.engine.core.actions.ActionList;
import com.automation.engine.core.conditions.ConditionList;
import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.TriggerList;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.ObjectUtils;

@Data
@AllArgsConstructor
public class Automation {
    private final String alias;
    private final TriggerList triggers;
    private final ConditionList conditions;
    private final ActionList actions;

    /**
     * Check if any of the triggers are triggered
     *
     * @return true if any of the triggers are triggered
     */
    public boolean anyTriggerActivated(Event event) {
        if (ObjectUtils.isEmpty(triggers)) return false;
        return triggers.anyTriggered(event);
    }

    /**
     * Check if all conditions are met
     *
     * @return true if all conditions are met
     */
    public boolean allConditionsMet(Event context) {
        if (ObjectUtils.isEmpty(conditions)) return true;
        return conditions.allSatisfied(context);
    }

    /**
     * Perform all actions
     */
    public void performActions(Event context) {
        if (ObjectUtils.isEmpty(actions)) return;
        actions.executeAll(context);
    }
}