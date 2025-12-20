package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

import java.util.List;
import java.util.Map;

public interface TypedAction<T extends IActionContext> extends IAction {
    ITypeConverter getTypeConverter();

    TypedAction<T> getSelf();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default void execute(EventContext eventContext, ActionContext actionContext) {
        T data = getTypeConverter().convert(actionContext, this.getContextType());
        if (data == null)
            throw new IllegalArgumentException("Cannot convert action context data to " + this.getContextType());

        // Calling the proxied self to ensure AOP aspects are applied such as transactions, logging, etc.
        var self = getSelf();
        if (self == null)
            throw new IllegalStateException("Self reference is not initialized");
        if (self.canExecute(eventContext, data)) {
            self.doExecute(eventContext, data);
        }
    }

    default boolean canExecute(EventContext ec, T ac) {
        return true;
    }

    void doExecute(EventContext ec, T ac);

    @Override
    default List<T> getExamples() {
        var example = getTypeConverter().convert(Map.of(), getContextType());
        return List.of((T) example);
    }
}