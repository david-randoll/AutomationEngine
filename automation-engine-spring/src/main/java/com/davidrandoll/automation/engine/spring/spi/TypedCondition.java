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

    TypedCondition<T> getSelf();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default boolean isSatisfied(EventContext eventContext, ConditionContext conditionContext) {
        T data = getTypeConverter().convert(conditionContext, getContextType());
        // Calling the proxied self to ensure AOP aspects are applied such as transactions, logging, etc.
        var self = getSelf();
        if (self == null)
            throw new IllegalStateException("Self reference is not initialized");
        return self.isSatisfied(eventContext, data);
    }

    boolean isSatisfied(EventContext ec, T cc);

    @Override
    default List<T> getExamples() {
        var example = getTypeConverter().convert(Map.of(), getContextType());
        return List.of((T) example);
    }
}
