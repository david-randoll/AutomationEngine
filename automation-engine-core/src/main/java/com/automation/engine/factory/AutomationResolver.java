package com.automation.engine.factory;

import com.automation.engine.core.actions.BaseActionList;
import com.automation.engine.core.conditions.BaseConditionList;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.triggers.BaseTriggerList;
import com.automation.engine.core.variables.BaseVariableList;
import com.automation.engine.factory.actions.Action;
import com.automation.engine.factory.actions.ActionResolver;
import com.automation.engine.factory.conditions.Condition;
import com.automation.engine.factory.conditions.ConditionResolver;
import com.automation.engine.factory.triggers.Trigger;
import com.automation.engine.factory.triggers.TriggerResolver;
import com.automation.engine.factory.variables.Variable;
import com.automation.engine.factory.variables.VariableResolver;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class AutomationResolver {
    private final ActionResolver actionResolver;
    private final ConditionResolver conditionResolver;
    private final VariableResolver variableResolver;
    private final TriggerResolver triggerResolver;

    /*
        Actions
     */
    public BaseActionList resolveActions(List<Action> actions) {
        return actionResolver.resolve(actions);
    }

    public void executeActions(EventContext eventContext, List<Action> actions) {
        actionResolver.executeActions(eventContext, actions);
    }

    public void executeActionsAsync(EventContext eventContext, List<Action> actions) {
        actionResolver.executeActionsAsync(eventContext, actions);
    }

    public void executeActionsAsync(EventContext eventContext, List<Action> actions, Executor executor) {
        actionResolver.executeActionsAsync(eventContext, actions, executor);
    }

    /*
        Conditions
     */
    public BaseConditionList resolveConditions(List<Condition> conditions) {
        return conditionResolver.resolve(conditions);
    }

    public boolean allConditionsSatisfied(EventContext eventContext, List<Condition> conditions) {
        return conditionResolver.allConditionsSatisfied(eventContext, conditions);
    }

    public boolean anyConditionSatisfied(EventContext eventContext, List<Condition> conditions) {
        return conditionResolver.anyConditionSatisfied(eventContext, conditions);
    }

    public boolean noneConditionSatisfied(EventContext eventContext, List<Condition> conditions) {
        return conditionResolver.noneConditionSatisfied(eventContext, conditions);
    }

    /*
        Triggers
     */
    public BaseTriggerList resolveTriggers(List<Trigger> triggers) {
        return triggerResolver.resolve(triggers);
    }

    public boolean anyTriggersTriggered(EventContext eventContext, List<Trigger> triggers) {
        return triggerResolver.anyTriggersTriggered(eventContext, triggers);
    }

    public boolean allTriggersTriggered(EventContext eventContext, List<Trigger> triggers) {
        return triggerResolver.allTriggersTriggered(eventContext, triggers);
    }

    public boolean noneTriggersTriggered(EventContext eventContext, List<Trigger> triggers) {
        return triggerResolver.noneTriggersTriggered(eventContext, triggers);
    }

    /*
        Variables
     */
    public BaseVariableList resolveVariables(List<Variable> variables) {
        return variableResolver.resolve(variables);
    }

    public void resolveVariables(EventContext eventContext, List<Variable> variables) {
        variableResolver.resolveVariables(eventContext, variables);
    }
}