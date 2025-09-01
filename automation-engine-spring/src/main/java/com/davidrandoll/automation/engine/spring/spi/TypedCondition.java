package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

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
}
