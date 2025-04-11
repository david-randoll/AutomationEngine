package com.automation.engine.spi;

import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.utils.GenericTypeResolver;

public interface TypedAction<T extends IActionContext> extends IAction {
    ITypeConverter getTypeConverter();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default void execute(EventContext eventContext, ActionContext actionContext) {
        T data = getTypeConverter().convert(actionContext.getData(), getContextType());
        execute(eventContext, data);
    }

    void execute(EventContext eventContext, T actionContext);
}