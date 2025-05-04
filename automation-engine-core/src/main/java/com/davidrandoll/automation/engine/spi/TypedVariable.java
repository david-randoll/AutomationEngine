package com.davidrandoll.automation.engine.spi;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.davidrandoll.automation.engine.core.variables.VariableContext;

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