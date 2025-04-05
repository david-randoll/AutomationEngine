package com.automation.engine.core.variables;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.ITypeConverter;

public abstract class BaseAbstractVariable<T extends IVariableContext> implements IVariable {
    protected abstract ITypeConverter getTypeConverter();

    @Override
    public Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    public void resolve(EventContext eventContext, VariableContext variableContext) {
        T data = getTypeConverter().convert(variableContext.getData(), getContextType());
        resolve(eventContext, data);
    }

    public abstract void resolve(EventContext eventContext, T variableContext);
}