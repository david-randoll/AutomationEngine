package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;
import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.BaseActionList;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.actions.IBaseAction;
import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.actions.interceptors.InterceptingAction;
import com.automation.engine.core.conditions.ConditionContext;
import com.automation.engine.core.conditions.BaseConditionList;
import com.automation.engine.core.conditions.IBaseCondition;
import com.automation.engine.core.conditions.ICondition;
import com.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.automation.engine.core.conditions.interceptors.InterceptingCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.IBaseTrigger;
import com.automation.engine.core.triggers.ITrigger;
import com.automation.engine.core.triggers.TriggerContext;
import com.automation.engine.core.triggers.BaseTriggerList;
import com.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.core.triggers.interceptors.InterceptingTrigger;
import com.automation.engine.factory.exceptions.ActionNotFoundException;
import com.automation.engine.factory.exceptions.ConditionNotFoundException;
import com.automation.engine.factory.exceptions.TriggerNotFoundException;
import com.automation.engine.factory.request.Action;
import com.automation.engine.factory.request.Condition;
import com.automation.engine.factory.request.CreateRequest;
import com.automation.engine.factory.request.Trigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service("manualAutomationResolver")
@RequiredArgsConstructor
public class DefaultAutomationResolver implements IAutomationResolver<CreateRequest> {
    private final ApplicationContext applicationContext;

    private final List<ITriggerInterceptor> triggerInterceptors;
    private final List<IConditionInterceptor> conditionInterceptors;
    private final List<IActionInterceptor> actionInterceptors;

    @Override
    @NonNull
    public Automation create(CreateRequest createRequest) {
        log.info("Start creating automation: {}", createRequest.getAlias());
        var alias = createRequest.getAlias();
        BaseTriggerList triggers = buildTriggersList(createRequest.getTriggers());
        BaseConditionList conditions = buildConditionsList(createRequest.getConditions());
        BaseActionList actions = buildActionsList(createRequest.getActions());
        var automation = new Automation(alias, triggers, conditions, actions);
        log.info("Automation {} created successfully", alias);
        return automation;
    }

    @NonNull
    public BaseTriggerList buildTriggersList(List<Trigger> triggers) {
        var result = new BaseTriggerList();

        if (ObjectUtils.isEmpty(triggers)) return result;

        var triggersMap = getMap(ITrigger.class);
        for (var trigger : triggers) {
            var triggerName = "%sTrigger".formatted(trigger.getTrigger());
            ITrigger triggerInstance = Optional.ofNullable(triggersMap.get(triggerName))
                    .orElseThrow(() -> new TriggerNotFoundException(trigger.getTrigger()));

            var interceptingTrigger = new InterceptingTrigger(triggerInstance, triggerInterceptors);
            var triggerContext = new TriggerContext(trigger.getParams());

            IBaseTrigger newTriggerInstance = event -> interceptingTrigger.isTriggered(event, triggerContext);

            result.add(newTriggerInstance);
        }

        return result;
    }

    @NonNull
    public BaseConditionList buildConditionsList(List<Condition> conditions) {
        var result = new BaseConditionList();

        if (ObjectUtils.isEmpty(conditions)) return result;

        var conditionsMap = getMap(ICondition.class);
        for (var condition : conditions) {
            var conditionName = "%sCondition".formatted(condition.getCondition());
            ICondition conditionInstance = Optional.ofNullable(conditionsMap.get(conditionName))
                    .orElseThrow(() -> new ConditionNotFoundException(condition.getCondition()));

            var interceptingCondition = new InterceptingCondition(conditionInstance, conditionInterceptors);
            var conditionContext = new ConditionContext(condition.getParams());

            IBaseCondition newConditionInstance = eventContext -> interceptingCondition.isSatisfied(eventContext, conditionContext);

            result.add(newConditionInstance);
        }

        return result;
    }

    public BaseActionList buildActionsList(List<Action> actions) {
        var result = new BaseActionList();

        if (ObjectUtils.isEmpty(actions)) return result;

        var actionsMap = getMap(IAction.class);
        for (var action : actions) {
            var actionName = "%sAction".formatted(action.getAction());
            IAction actionInstance = Optional.ofNullable(actionsMap.get(actionName))
                    .orElseThrow(() -> new ActionNotFoundException(action.getAction()));

            var interceptingAction = new InterceptingAction(actionInstance, actionInterceptors);
            var actionContext = new ActionContext(action.getParams());

            IBaseAction newActionInstance = eventContext -> interceptingAction.execute(eventContext, actionContext);

            result.add(newActionInstance);
        }

        return result;
    }

    public boolean allConditionsSatisfied(Event eventContext, @Nullable List<Condition> conditions) {
        BaseConditionList resolvedConditions = buildConditionsList(conditions);
        return resolvedConditions.allSatisfied(eventContext);
    }

    public boolean anyConditionSatisfied(Event eventContext, @Nullable List<Condition> conditions) {
        BaseConditionList resolvedConditions = buildConditionsList(conditions);
        return resolvedConditions.anySatisfied(eventContext);
    }

    public boolean noneConditionSatisfied(Event eventContext, @Nullable List<Condition> conditions) {
        BaseConditionList resolvedConditions = buildConditionsList(conditions);
        return resolvedConditions.noneSatisfied(eventContext);
    }

    public void executeActions(Event eventContext, @Nullable List<Action> actions) {
        if (ObjectUtils.isEmpty(actions)) return;
        BaseActionList resolvedActions = buildActionsList(actions);
        resolvedActions.executeAll(eventContext);
    }

    @NonNull
    private <T> Map<String, T> getMap(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz);
    }
}