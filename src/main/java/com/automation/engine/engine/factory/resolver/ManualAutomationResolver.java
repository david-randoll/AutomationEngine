package com.automation.engine.engine.factory.resolver;

import com.automation.engine.engine.core.Automation;
import com.automation.engine.engine.core.actions.ActionContext;
import com.automation.engine.engine.core.actions.IAction;
import com.automation.engine.engine.core.actions.IBaseAction;
import com.automation.engine.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.engine.core.actions.interceptors.InterceptingAction;
import com.automation.engine.engine.core.conditions.ConditionContext;
import com.automation.engine.engine.core.conditions.IBaseCondition;
import com.automation.engine.engine.core.conditions.ICondition;
import com.automation.engine.engine.core.conditions.interceptors.IConditionInterceptor;
import com.automation.engine.engine.core.conditions.interceptors.InterceptingCondition;
import com.automation.engine.engine.core.triggers.IBaseTrigger;
import com.automation.engine.engine.core.triggers.ITrigger;
import com.automation.engine.engine.core.triggers.TriggerContext;
import com.automation.engine.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.engine.core.triggers.interceptors.InterceptingTrigger;
import com.automation.engine.engine.factory.CreateAutomation;
import com.automation.engine.engine.factory.exceptions.ActionNotFoundException;
import com.automation.engine.engine.factory.exceptions.ConditionNotFoundException;
import com.automation.engine.engine.factory.exceptions.TriggerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualAutomationResolver {
    private final Map<String, ITrigger> triggersMap;
    private final Map<String, ICondition> conditionsMap;
    private final Map<String, IAction> actionsMap;

    private final List<ITriggerInterceptor> triggerInterceptors;
    private final List<IConditionInterceptor> conditionInterceptors;
    private final List<IActionInterceptor> actionInterceptors;

    public Automation createAutomation(CreateAutomation createAutomation) {
        log.info("Start creating automation: {}", createAutomation.getAlias());
        var alias = createAutomation.getAlias();
        List<IBaseTrigger> triggers = createTriggers(createAutomation.getTriggers());
        List<IBaseCondition> conditions = createConditions(createAutomation.getConditions());
        List<IBaseAction> actions = createActions(createAutomation.getActions());
        var automation = new Automation(alias, triggers, conditions, actions);
        log.info("Automation {} created successfully", alias);
        return automation;
    }

    private List<IBaseTrigger> createTriggers(List<CreateAutomation.Trigger> triggers) {
        var result = new ArrayList<IBaseTrigger>();

        if (ObjectUtils.isEmpty(triggers)) return result;

        for (var trigger : triggers) {
            ITrigger triggerInstance = Optional.ofNullable(triggersMap.get(trigger.getTrigger()))
                    .orElseThrow(() -> new TriggerNotFoundException(trigger.getTrigger()));

            var interceptingTrigger = new InterceptingTrigger(triggerInstance, triggerInterceptors);
            var triggerContext = new TriggerContext(trigger.getParams());

            IBaseTrigger newTriggerInstance = event -> interceptingTrigger.isTriggered(event, triggerContext);

            result.add(newTriggerInstance);
        }

        return result;
    }

    private List<IBaseCondition> createConditions(List<CreateAutomation.Condition> conditions) {
        var result = new ArrayList<IBaseCondition>();

        if (ObjectUtils.isEmpty(conditions)) return result;

        for (var condition : conditions) {
            ICondition conditionInstance = Optional.ofNullable(conditionsMap.get(condition.getCondition()))
                    .orElseThrow(() -> new ConditionNotFoundException(condition.getCondition()));

            var interceptingCondition = new InterceptingCondition(conditionInstance, conditionInterceptors);
            var conditionContext = new ConditionContext(condition.getParams());

            IBaseCondition newConditionInstance = eventContext -> interceptingCondition.isSatisfied(eventContext, conditionContext);

            result.add(newConditionInstance);
        }

        return result;
    }

    private List<IBaseAction> createActions(List<CreateAutomation.Action> actions) {
        var result = new ArrayList<IBaseAction>();

        if (ObjectUtils.isEmpty(actions)) return result;

        for (var action : actions) {
            IAction actionInstance = Optional.ofNullable(actionsMap.get(action.getAction()))
                    .orElseThrow(() -> new ActionNotFoundException(action.getAction()));

            var interceptingAction = new InterceptingAction(actionInstance, actionInterceptors);
            var actionContext = new ActionContext(action.getParams());

            IBaseAction newActionInstance = eventContext -> interceptingAction.execute(eventContext, actionContext);

            result.add(newActionInstance);
        }

        return result;
    }
}