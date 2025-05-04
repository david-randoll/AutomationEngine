package com.davidrandoll.automation.engine.spi;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

public interface TypedTrigger<T extends ITriggerContext> extends ITrigger {
    ITypeConverter getTypeConverter();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default boolean isTriggered(EventContext eventContext, TriggerContext triggerContext) {
        T data = getTypeConverter().convert(triggerContext.getData(), getContextType());
        return isTriggered(eventContext, data);
    }

    boolean isTriggered(EventContext ec, T tc);
}