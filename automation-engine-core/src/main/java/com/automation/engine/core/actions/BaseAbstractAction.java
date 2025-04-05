package com.automation.engine.core.actions;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.ITypeConverter;

public abstract class BaseAbstractAction<T extends IActionContext> implements IAction {
    protected abstract ITypeConverter getTypeConverter();

    @Override
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public void execute(EventContext eventContext, ActionContext actionContext) {
        T data = getTypeConverter().convert(actionContext.getData(), getContextType());
        execute(eventContext, data);
    }

    public abstract void execute(EventContext eventContext, T actionContext);
}