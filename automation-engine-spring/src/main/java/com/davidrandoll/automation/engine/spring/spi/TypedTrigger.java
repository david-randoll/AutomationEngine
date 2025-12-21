package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

import java.util.List;
import java.util.Map;

public interface TypedTrigger<T extends ITriggerContext> extends ITrigger {
    ITypeConverter getTypeConverter();

    TypedTrigger<T> getSelf();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default boolean isTriggered(EventContext eventContext, TriggerContext triggerContext) {
        T data = getTypeConverter().convert(triggerContext, getContextType());
        // Calling the proxied self to ensure AOP aspects are applied such as transactions, logging, etc.
        var self = getSelf();
        if (self == null)
            throw new IllegalStateException("Self reference is not initialized");
        return self.isTriggered(eventContext, data);
    }

    boolean isTriggered(EventContext ec, T tc);

    @Override
    default List<T> getExamples() {
        var example = getTypeConverter().convert(Map.of(), getContextType());
        return List.of((T) example);
    }
}