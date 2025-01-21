package com.automation.engine.engine.conditions;

import com.automation.engine.engine.events.EventContext;
import com.automation.engine.engine.utils.GenericTypeResolver;
import com.automation.engine.engine.utils.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCondition<T> implements ICondition {
    @Autowired
    private TypeConverter typeConverter;

    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public boolean isMet(EventContext eventContext, ConditionContext conditionContext) {
        try {
            T data = typeConverter.convert(eventContext.getData(), getContextType());
            return isMet(eventContext, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute condition", e);
        }
    }

    public abstract boolean isMet(EventContext context, T conditionContext);
}
