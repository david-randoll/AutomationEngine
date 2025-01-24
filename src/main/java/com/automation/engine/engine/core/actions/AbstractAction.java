package com.automation.engine.engine.core.actions;

import com.automation.engine.engine.core.events.EventContext;
import com.automation.engine.engine.core.utils.GenericTypeResolver;
import com.automation.engine.engine.core.utils.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

public abstract class AbstractAction<T> implements IAction {
    @Autowired
    private TypeConverter typeConverter;

    @Override
    @NonNull
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public void execute(EventContext eventContext, ActionContext actionContext) {
        try {
            T data = typeConverter.convert(actionContext.getData(), getContextType());
            execute(eventContext, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute action", e);
        }
    }

    public abstract void execute(EventContext eventContext, T actionContext);
}