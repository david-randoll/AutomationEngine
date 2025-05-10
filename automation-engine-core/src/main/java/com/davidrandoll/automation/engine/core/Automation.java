package com.davidrandoll.automation.engine.core;

import com.davidrandoll.automation.engine.core.actions.BaseActionList;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopAutomationException;
import com.davidrandoll.automation.engine.core.conditions.BaseConditionList;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.IBaseResult;
import com.davidrandoll.automation.engine.core.triggers.BaseTriggerList;
import com.davidrandoll.automation.engine.core.variables.BaseVariableList;
import lombok.Getter;

import java.util.Optional;

import static java.util.Objects.isNull;

@Getter
public final class Automation {
    private final String alias;
    private final BaseVariableList variables;
    private final BaseTriggerList triggers;
    private final BaseConditionList conditions;
    private final BaseActionList actions;
    private final IBaseResult result;

    public Automation(String alias, BaseVariableList variables, BaseTriggerList triggers, BaseConditionList conditions, BaseActionList actions, IBaseResult result) {
        this.alias = alias;
        this.variables = Optional.ofNullable(variables).orElse(BaseVariableList.of());
        this.triggers = Optional.ofNullable(triggers).orElse(BaseTriggerList.of());
        this.conditions = Optional.ofNullable(conditions).orElse(BaseConditionList.of());
        this.actions = Optional.ofNullable(actions).orElse(BaseActionList.of());
        this.result = Optional.ofNullable(result).orElse(context -> null);
    }

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

    public Automation(Automation automation) {
        this(
                automation.alias,
                BaseVariableList.of(automation.variables),
                BaseTriggerList.of(automation.triggers),
                BaseConditionList.of(automation.conditions),
                BaseActionList.of(automation.actions),
                automation.result
        );
    }

    public Object getExecutionSummary(EventContext context) {
        return result.getExecutionSummary(context);
    }
}