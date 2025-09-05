package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

import java.util.List;

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