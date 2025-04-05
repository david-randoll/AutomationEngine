package com.automation.engine.core.conditions;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.ITypeConverter;

public abstract class BaseAbstractCondition<T extends IConditionContext> implements ICondition {
    protected abstract ITypeConverter getTypeConverter();

    @Override
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public boolean isSatisfied(EventContext eventContext, ConditionContext conditionContext) {
        T data = getTypeConverter().convert(conditionContext.getData(), getContextType());
        return isSatisfied(eventContext, data);
    }

    public abstract boolean isSatisfied(EventContext eventContext, T conditionContext);
}
