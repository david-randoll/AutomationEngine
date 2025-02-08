package com.automation.engine.core.triggers;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

public abstract class AbstractTrigger<T extends ITriggerContext> implements ITrigger {
    @Autowired
    private TypeConverter typeConverter;

    @Override
    @NonNull
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public boolean isTriggered(Event event, TriggerContext context) {
        T data = typeConverter.convert(context.getData(), getContextType());
        return isTriggered(event, data);
    }

    public abstract boolean isTriggered(Event event, T context);
}
