package com.automation.engine.core.actions;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.ITypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

public abstract class AbstractAction<T extends IActionContext> implements IAction {
    @Autowired
    private ITypeConverter typeConverter;

    @Override
    @NonNull
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public void execute(EventContext eventContext, ActionContext actionContext) {
        T data = typeConverter.convert(actionContext.getData(), getContextType());
        execute(eventContext, data);
    }

    public abstract void execute(EventContext eventContext, T actionContext);
}