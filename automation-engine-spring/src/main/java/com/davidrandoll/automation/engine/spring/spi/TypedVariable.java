package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.davidrandoll.automation.engine.core.variables.VariableContext;

import java.util.List;
import java.util.Map;

public interface TypedVariable<T extends IVariableContext> extends IVariable {
    ITypeConverter getTypeConverter();

    TypedVariable<T> getSelf();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default void resolve(EventContext eventContext, VariableContext variableContext) {
        T data = getTypeConverter().convert(variableContext, getContextType());
        // Calling the proxied self to ensure AOP aspects are applied such as transactions, logging, etc.
        var self = getSelf();
        if (self == null)
            throw new IllegalStateException("Self reference is not initialized");
        self.resolve(eventContext, data);
    }

    void resolve(EventContext eventContext, T variableContext);

    @Override
    default List<T> getExamples() {
        var example = getTypeConverter().convert(Map.of(), getContextType());
        return List.of((T) example);
    }
}