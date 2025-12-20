package com.davidrandoll.automation.engine.creator.conditions;

import com.davidrandoll.automation.engine.core.conditions.BaseConditionList;
import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.IBaseCondition;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.conditions.interceptors.InterceptingCondition;
import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class ConditionBuilder {
    private final IConditionSupplier supplier;
    private final List<IConditionInterceptor> conditionInterceptors;

    public BaseConditionList resolve(List<ConditionDefinition> conditions) {
        var result = new BaseConditionList();

        if (isNull(conditions)) return result;

        for (var condition : conditions) {
            IBaseCondition newConditionInstance = buildCondition(condition);

            result.add(newConditionInstance);
        }

        return result;
    }

    private IBaseCondition buildCondition(ConditionDefinition condition) {
        ICondition conditionInstance = Optional.ofNullable(supplier.getCondition(condition.getCondition()))
                .orElseThrow(() -> new ConditionNotFoundException(condition.getCondition()));

        var interceptingCondition = new InterceptingCondition(conditionInstance, conditionInterceptors);
        
        // Add alias, description, and type to params map for tracing interceptors
        Map<String, Object> params = new HashMap<>(condition.getParams());
        params.put("__type", condition.getCondition()); // Store type for tracing interceptors
        if (condition.getAlias() != null) {
            params.put("alias", condition.getAlias());
        }
        if (condition.getDescription() != null) {
            params.put("description", condition.getDescription());
        }
        
        var conditionContext = new ConditionContext(params);

        return eventContext -> interceptingCondition.isSatisfied(eventContext, conditionContext);
    }

    public boolean allConditionsSatisfied(EventContext eventContext, List<ConditionDefinition> conditions) {
        BaseConditionList resolvedConditions = resolve(conditions);
        return resolvedConditions.allSatisfied(eventContext);
    }

    public boolean anyConditionSatisfied(EventContext eventContext, List<ConditionDefinition> conditions) {
        BaseConditionList resolvedConditions = resolve(conditions);
        return resolvedConditions.anySatisfied(eventContext);
    }

    public boolean noneConditionSatisfied(EventContext eventContext, List<ConditionDefinition> conditions) {
        BaseConditionList resolvedConditions = resolve(conditions);
        return resolvedConditions.noneSatisfied(eventContext);
    }
}