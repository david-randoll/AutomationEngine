package com.automation.engine.core.actions;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.events.Event;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

public abstract class AbstractAction<T extends IActionContext> implements IAction {
    @Autowired
    private TypeConverter typeConverter;

    @Override
    @NonNull
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public void execute(Event eventContext, ActionContext actionContext) throws StopActionSequenceException {
        T data = typeConverter.convert(actionContext.getData(), getContextType());
        execute(eventContext, data);
    }

    public abstract void execute(Event event, T context) throws StopActionSequenceException;
}