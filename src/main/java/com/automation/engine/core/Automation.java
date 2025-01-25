package com.automation.engine.core;

import com.automation.engine.core.actions.IBaseAction;
import com.automation.engine.core.conditions.IBaseCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.triggers.IBaseTrigger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
public class Automation {
    private final String alias;
    private final List<IBaseTrigger> triggers;
    private final List<IBaseCondition> conditions;
    private final List<IBaseAction> actions;

    /**
     * Check if any of the triggers are triggered
     *
     * @return true if any of the triggers are triggered
     */
    public boolean isTriggered(Event event) {
        if (ObjectUtils.isEmpty(triggers)) return false;
        return triggers.stream()
                .anyMatch(trigger -> trigger.isTriggered(event));
    }

    /**
     * Check if all conditions are met
     *
     * @return true if all conditions are met
     */
    public boolean areConditionsMet(EventContext context) {
        if (ObjectUtils.isEmpty(conditions)) return true;
        return conditions.stream()
                .allMatch(condition -> condition.isSatisfied(context));
    }

    /**
     * Execute all actions
     */
    public void executeActions(EventContext context) {
        if (ObjectUtils.isEmpty(actions)) return;
        actions.forEach(action -> action.execute(context));
    }
}