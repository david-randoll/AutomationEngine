package com.davidrandoll.automation.engine.creator;

import com.davidrandoll.automation.engine.core.actions.BaseActionList;
import com.davidrandoll.automation.engine.core.conditions.BaseConditionList;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.IBaseResult;
import com.davidrandoll.automation.engine.core.triggers.BaseTriggerList;
import com.davidrandoll.automation.engine.core.variables.BaseVariableList;
import com.davidrandoll.automation.engine.creator.actions.Action;
import com.davidrandoll.automation.engine.creator.actions.ActionBuilder;
import com.davidrandoll.automation.engine.creator.conditions.Condition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionBuilder;
import com.davidrandoll.automation.engine.creator.result.Result;
import com.davidrandoll.automation.engine.creator.result.ResultBuilder;
import com.davidrandoll.automation.engine.creator.triggers.Trigger;
import com.davidrandoll.automation.engine.creator.triggers.TriggerBuilder;
import com.davidrandoll.automation.engine.creator.variables.Variable;
import com.davidrandoll.automation.engine.creator.variables.VariableBuilder;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class AutomationProcessor {
    private final ActionBuilder actionBuilder;
    private final ConditionBuilder conditionBuilder;
    private final TriggerBuilder triggerBuilder;
    private final VariableBuilder variableBuilder;
    private final ResultBuilder resultBuilder;

    /*
        Actions
     */
    public BaseActionList resolveActions(List<Action> actions) {
        return actionBuilder.resolve(actions);
    }

    public void executeActions(EventContext eventContext, List<Action> actions) {
        actionBuilder.executeActions(eventContext, actions);
    }

    public void executeActionsAsync(EventContext eventContext, List<Action> actions) {
        actionBuilder.executeActionsAsync(eventContext, actions);
    }

    public void executeActionsAsync(EventContext eventContext, List<Action> actions, Executor executor) {
        actionBuilder.executeActionsAsync(eventContext, actions, executor);
    }

    /*
        Conditions
     */
    public BaseConditionList resolveConditions(List<Condition> conditions) {
        return conditionBuilder.resolve(conditions);
    }

    public boolean allConditionsSatisfied(EventContext eventContext, List<Condition> conditions) {
        return conditionBuilder.allConditionsSatisfied(eventContext, conditions);
    }

    public boolean anyConditionSatisfied(EventContext eventContext, List<Condition> conditions) {
        return conditionBuilder.anyConditionSatisfied(eventContext, conditions);
    }

    public boolean noneConditionSatisfied(EventContext eventContext, List<Condition> conditions) {
        return conditionBuilder.noneConditionSatisfied(eventContext, conditions);
    }

    /*
        Triggers
     */
    public BaseTriggerList resolveTriggers(List<Trigger> triggers) {
        return triggerBuilder.resolve(triggers);
    }

    public boolean anyTriggersTriggered(EventContext eventContext, List<Trigger> triggers) {
        return triggerBuilder.anyTriggersTriggered(eventContext, triggers);
    }

    public boolean allTriggersTriggered(EventContext eventContext, List<Trigger> triggers) {
        return triggerBuilder.allTriggersTriggered(eventContext, triggers);
    }

    public boolean noneTriggersTriggered(EventContext eventContext, List<Trigger> triggers) {
        return triggerBuilder.noneTriggersTriggered(eventContext, triggers);
    }

    /*
        Variables
     */
    public BaseVariableList resolveVariables(List<Variable> variables) {
        return variableBuilder.resolve(variables);
    }

    public void resolveVariables(EventContext eventContext, List<Variable> variables) {
        variableBuilder.resolveVariables(eventContext, variables);
    }

    /*
        Results
     */
    public IBaseResult resolveResult(Result result) {
        return resultBuilder.resolve(result);
    }

    public Object resolveResult(EventContext eventContext, Result result) {
        return resultBuilder.resolveResult(eventContext, result);
    }
}