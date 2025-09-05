package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

import java.util.List;

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

    @Override
    default List<T> getExamples() {
        var contextType = getContextType();
        try {
            var example = contextType.getDeclaredConstructor().newInstance();
            return List.of((T) example);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create example instance of " + contextType, e);
        }
    }
}