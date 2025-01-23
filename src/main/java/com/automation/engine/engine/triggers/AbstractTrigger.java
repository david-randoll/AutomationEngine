package com.automation.engine.engine.triggers;

import com.automation.engine.engine.events.Event;
import com.automation.engine.engine.utils.GenericTypeResolver;
import com.automation.engine.engine.utils.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

public abstract class AbstractTrigger<T> implements ITrigger {
    @Autowired
    private TypeConverter typeConverter;

    @Override
    @NonNull
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public boolean isTriggered(Event event, TriggerContext triggerContext) {
        try {
            T data = typeConverter.convert(triggerContext.getData(), getContextType());
            return isTriggered(event, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert trigger context data", e);
        }
    }

    public abstract boolean isTriggered(Event event, T triggerContext);
}
