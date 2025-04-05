package com.automation.engine.core.triggers;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.ITypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

public abstract class AbstractTrigger<T extends ITriggerContext> implements ITrigger {
    @Autowired
    private ITypeConverter typeConverter;

    @Override
    @NonNull
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public boolean isTriggered(EventContext eventContext, TriggerContext triggerContext) {
        T data = typeConverter.convert(triggerContext.getData(), getContextType());
        return isTriggered(eventContext, data);
    }

    public abstract boolean isTriggered(EventContext eventContext, T triggerContext);
}
