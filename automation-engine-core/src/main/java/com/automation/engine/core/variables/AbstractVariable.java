package com.automation.engine.core.variables;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

public abstract class AbstractVariable<T extends IVariableContext> implements IVariable {
    @Autowired
    private TypeConverter typeConverter;

    @Override
    @NonNull
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public void setVariable(Event eventContext, VariableContext variableContext) {
        T data = typeConverter.convert(variableContext.getData(), getContextType());
        setVariable(eventContext, data);
    }

    public abstract void setVariable(Event event, T context);
}