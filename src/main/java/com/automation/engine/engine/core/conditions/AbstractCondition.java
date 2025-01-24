package com.automation.engine.engine.core.conditions;

import com.automation.engine.engine.core.events.EventContext;
import com.automation.engine.engine.core.utils.GenericTypeResolver;
import com.automation.engine.engine.core.utils.TypeConverter;
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
    public boolean isSatisfied(EventContext eventContext, ConditionContext conditionContext) {
        try {
            T data = typeConverter.convert(eventContext.getData(), getContextType());
            return isMet(eventContext, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute condition", e);
        }
    }

    public abstract boolean isMet(EventContext context, T conditionContext);
}
