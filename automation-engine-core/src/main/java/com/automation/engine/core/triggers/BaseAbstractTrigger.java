package com.automation.engine.core.triggers;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.ITypeConverter;

public abstract class BaseAbstractTrigger<T extends ITriggerContext> implements ITrigger {
    protected abstract ITypeConverter getTypeConverter();

    @Override
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public boolean isTriggered(EventContext eventContext, TriggerContext triggerContext) {
        T data = getTypeConverter().convert(triggerContext.getData(), getContextType());
        return isTriggered(eventContext, data);
    }

    public abstract boolean isTriggered(EventContext eventContext, T triggerContext);
}
