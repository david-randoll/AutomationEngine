package com.automation.engine.spi;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.utils.GenericTypeResolver;
import com.automation.engine.core.utils.ITypeConverter;
import com.automation.engine.core.variables.IVariable;
import com.automation.engine.core.variables.IVariableContext;
import com.automation.engine.core.variables.VariableContext;

public interface TypedVariable<T extends IVariableContext> extends IVariable {
    ITypeConverter getTypeConverter();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default void resolve(EventContext eventContext, VariableContext variableContext) {
        T data = getTypeConverter().convert(variableContext.getData(), getContextType());
        resolve(eventContext, data);
    }

    void resolve(EventContext eventContext, T variableContext);
}