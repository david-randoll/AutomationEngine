package com.automation.engine.engine.actions;

import com.automation.engine.engine.events.EventContext;
import com.automation.engine.engine.utils.GenericTypeResolver;
import com.automation.engine.engine.utils.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAction<T> implements IAction {
    @Autowired
    private TypeConverter typeConverter;

    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public void execute(EventContext context, ActionContext actionContext) {
        try {
            T data = typeConverter.convert(actionContext.getData(), getContextType());
            execute(context, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute action", e);
        }
    }

    public abstract void execute(EventContext eventContext, T actionContext);
}