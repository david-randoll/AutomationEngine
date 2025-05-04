package com.davidrandoll.automation.engine.spi;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

public interface TypedAction<T extends IActionContext> extends IAction {
    ITypeConverter getTypeConverter();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default void execute(EventContext eventContext, ActionContext actionContext) {
        T data = getTypeConverter().convert(actionContext.getData(), getContextType());
        if (data == null) {
            throw new IllegalArgumentException("Cannot convert action context data to " + getContextType());
        }
        if (canExecute(eventContext, data)) {
            doExecute(eventContext, data);
        }
    }

    default boolean canExecute(EventContext ec, T ac) {
        return true;
    }

    void doExecute(EventContext ec, T ac);
}