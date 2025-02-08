package com.automation.engine.core.conditions;

import com.automation.engine.core.events.Event;
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
    public boolean isSatisfied(Event event, ConditionContext context) {
        T data = typeConverter.convert(context.getData(), getContextType());
        return isSatisfied(event, data);
    }

    public abstract boolean isSatisfied(Event event, T context);
}
