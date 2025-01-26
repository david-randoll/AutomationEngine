package com.automation.engine.core.conditions;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

public abstract class AbstractCondition<T> implements ICondition {
    @Autowired
    private TypeConverter typeConverter;

    @Override
    @NonNull
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public boolean isSatisfied(Event eventContext, ConditionContext conditionContext) {
        try {
            T data = typeConverter.convert(conditionContext.getData(), getContextType());
            return isSatisfied(eventContext, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute condition", e);
        }
    }

    public abstract boolean isSatisfied(Event context, T conditionContext);
}
