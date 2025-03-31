package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;
import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.BaseActionList;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.actions.IBaseAction;
import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.actions.interceptors.InterceptingAction;
import com.automation.engine.core.conditions.BaseConditionList;
import com.automation.engine.core.conditions.ConditionContext;
import com.automation.engine.core.conditions.IBaseCondition;
import com.automation.engine.core.conditions.ICondition;
import com.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.automation.engine.core.conditions.interceptors.InterceptingCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.BaseTriggerList;
import com.automation.engine.core.triggers.IBaseTrigger;
import com.automation.engine.core.triggers.ITrigger;
import com.automation.engine.core.triggers.TriggerContext;
import com.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.core.triggers.interceptors.InterceptingTrigger;
import com.automation.engine.core.variables.BaseVariableList;
import com.automation.engine.core.variables.IBaseVariable;
import com.automation.engine.core.variables.IVariable;
import com.automation.engine.core.variables.VariableContext;
import com.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.automation.engine.core.variables.interceptors.InterceptingVariable;
import com.automation.engine.factory.exceptions.ActionNotFoundException;
import com.automation.engine.factory.exceptions.ConditionNotFoundException;
import com.automation.engine.factory.exceptions.TriggerNotFoundException;
import com.automation.engine.factory.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
@Service("manualAutomationResolver")
@RequiredArgsConstructor
public class DefaultAutomationResolver implements IAutomationResolver<CreateRequest> {
    private final ApplicationContext applicationContext;

    private final List<IVariableInterceptor> variableInterceptors;
    private final List<ITriggerInterceptor> triggerInterceptors;
    private final List<IConditionInterceptor> conditionInterceptors;
    private final List<IActionInterceptor> actionInterceptors;

    @Override
    @NonNull
    public Automation create(CreateRequest createRequest) {
        log.info("Start creating automation: {}", createRequest.getAlias());
        var alias = createRequest.getAlias();
        BaseVariableList variables = buildVariablesList(createRequest.getVariables());
        BaseTriggerList triggers = buildTriggersList(createRequest.getTriggers());
        BaseConditionList conditions = buildConditionsList(createRequest.getConditions());
        BaseActionList actions = buildActionsList(createRequest.getActions());
        var automation = new Automation(alias, variables, triggers, conditions, actions);
        log.info("Automation {} created successfully", alias);
        return automation;
    }

    @NonNull
    public BaseVariableList buildVariablesList(List<Variable> variables) {
        var result = new BaseVariableList();

        if (ObjectUtils.isEmpty(variables)) return result;

        for (Variable variable : variables) {
            IBaseVariable newVariableInstance = buildVariable(variable);
            result.add(newVariableInstance);
        }

        return result;
    }

    private IBaseVariable buildVariable(Variable variable) {
        var variableName = "%sVariable".formatted(variable.getVariable());
        IVariable variableInstance = Optional.ofNullable(getBean(variableName, IVariable.class))
                .orElseThrow(() -> new ActionNotFoundException(variable.getVariable()));

        var interceptingVariable = new InterceptingVariable(variableInstance, variableInterceptors);
        var variableContext = new VariableContext(variable.getParams());

        return event -> interceptingVariable.resolve(event, variableContext);
    }

    @NonNull
    public BaseTriggerList buildTriggersList(List<Trigger> triggers) {
        var result = new BaseTriggerList();

        if (ObjectUtils.isEmpty(triggers)) return result;

        for (Trigger trigger : triggers) {
            IBaseTrigger newTriggerInstance = buildTrigger(trigger);
            result.add(newTriggerInstance);
        }

        return result;
    }

    private IBaseTrigger buildTrigger(Trigger trigger) {
        var triggerName = "%sTrigger".formatted(trigger.getTrigger());
        ITrigger triggerInstance = Optional.ofNullable(getBean(triggerName, ITrigger.class))
                .orElseThrow(() -> new TriggerNotFoundException(trigger.getTrigger()));

        var interceptingTrigger = new InterceptingTrigger(triggerInstance, triggerInterceptors);
        var triggerContext = new TriggerContext(trigger.getParams());

        return event -> interceptingTrigger.isTriggered(event, triggerContext);
    }

    @NonNull
    public BaseConditionList buildConditionsList(List<Condition> conditions) {
        var result = new BaseConditionList();

        if (ObjectUtils.isEmpty(conditions)) return result;

        for (var condition : conditions) {
            IBaseCondition newConditionInstance = buildCondition(condition);

            result.add(newConditionInstance);
        }

        return result;
    }

    private IBaseCondition buildCondition(Condition condition) {
        var conditionName = "%sCondition".formatted(condition.getCondition());
        ICondition conditionInstance = Optional.ofNullable(getBean(conditionName, ICondition.class))
                .orElseThrow(() -> new ConditionNotFoundException(condition.getCondition()));

        var interceptingCondition = new InterceptingCondition(conditionInstance, conditionInterceptors);
        var conditionContext = new ConditionContext(condition.getParams());

        return eventContext -> interceptingCondition.isSatisfied(eventContext, conditionContext);
    }

    public BaseActionList buildActionsList(List<Action> actions) {
        var result = new BaseActionList();

        if (ObjectUtils.isEmpty(actions)) return result;

        for (var action : actions) {
            IBaseAction newActionInstance = buildAction(action);

            result.add(newActionInstance);
        }

        return result;
    }

    private IBaseAction buildAction(Action action) {
        var actionName = "%sAction".formatted(action.getAction());
        IAction actionInstance = Optional.ofNullable(getBean(actionName, IAction.class))
                .orElseThrow(() -> new ActionNotFoundException(action.getAction()));

        var interceptingAction = new InterceptingAction(actionInstance, actionInterceptors);
        var actionContext = new ActionContext(action.getParams());

        return eventContext -> interceptingAction.execute(eventContext, actionContext);
    }

    public boolean anyTriggersTriggered(Event event, @Nullable List<Trigger> triggers) {
        BaseTriggerList resolvedTriggers = buildTriggersList(triggers);
        return resolvedTriggers.anyTriggered(event);
    }

    public boolean allTriggersTriggered(Event event, @Nullable List<Trigger> triggers) {
        BaseTriggerList resolvedTriggers = buildTriggersList(triggers);
        return resolvedTriggers.allTriggered(event);
    }

    public boolean noneTriggersTriggered(Event event, @Nullable List<Trigger> triggers) {
        BaseTriggerList resolvedTriggers = buildTriggersList(triggers);
        return resolvedTriggers.noneTriggered(event);
    }

    public boolean allConditionsSatisfied(Event event, @Nullable List<Condition> conditions) {
        BaseConditionList resolvedConditions = buildConditionsList(conditions);
        return resolvedConditions.allSatisfied(event);
    }

    public boolean anyConditionSatisfied(Event event, @Nullable List<Condition> conditions) {
        BaseConditionList resolvedConditions = buildConditionsList(conditions);
        return resolvedConditions.anySatisfied(event);
    }

    public boolean noneConditionSatisfied(Event event, @Nullable List<Condition> conditions) {
        BaseConditionList resolvedConditions = buildConditionsList(conditions);
        return resolvedConditions.noneSatisfied(event);
    }

    public void executeActions(Event event, @Nullable List<Action> actions) {
        if (ObjectUtils.isEmpty(actions)) return;
        BaseActionList resolvedActions = buildActionsList(actions);
        resolvedActions.executeAll(event);
    }

    public void executeActionsAsync(Event event, @Nullable List<Action> actions) {
        if (ObjectUtils.isEmpty(actions)) return;
        BaseActionList resolvedActions = buildActionsList(actions);
        resolvedActions.executeAllAsync(event);
    }

    public void executeActionsAsync(Event event, @Nullable List<Action> actions, Executor executor) {
        if (ObjectUtils.isEmpty(actions)) return;
        BaseActionList resolvedActions = buildActionsList(actions);
        resolvedActions.executeAllAsync(event, executor);
    }

    @NonNull
    private <T> Map<String, T> getMap(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz);
    }

    public <T> T getBean(String name, Class<T> clazz) {
        try {
            return applicationContext.getBean(name, clazz);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            return null;
        }
    }

    public void resolveVariables(Event event, List<Variable> variables) {
        if (ObjectUtils.isEmpty(variables)) return;
        BaseVariableList resolvedVariables = buildVariablesList(variables);
        resolvedVariables.setAll(event);
    }
}