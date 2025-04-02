package com.automation.engine.core.conditions;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

public abstract class AbstractCondition<T extends IConditionContext> implements ICondition {
    @Autowired
    private TypeConverter typeConverter;

    @Override
    @NonNull
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public boolean isSatisfied(EventContext eventContext, ConditionContext conditionContext) {
        T data = typeConverter.convert(conditionContext.getData(), getContextType());
        return isSatisfied(eventContext, data);
    }

    public abstract boolean isSatisfied(EventContext eventContext, T conditionContext);
}
