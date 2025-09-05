package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

import java.util.List;
import java.util.Map;

public interface TypedCondition<T extends IConditionContext> extends ICondition {
    ITypeConverter getTypeConverter();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default boolean isSatisfied(EventContext eventContext, ConditionContext conditionContext) {
        T data = getTypeConverter().convert(conditionContext.getData(), getContextType());
        return isSatisfied(eventContext, data);
    }

    boolean isSatisfied(EventContext ec, T cc);

    @Override
    default List<T> getExamples() {
        var contextType = getContextType();
        var example = getTypeConverter().convert(Map.of(), contextType);
        return List.of((T) example);
    }
}
