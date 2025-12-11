package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.davidrandoll.automation.engine.creator.AutomationProcessor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class PluggableVariable<T extends IVariableContext> implements TypedVariable<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Autowired
    @Delegate
    protected AutomationProcessor processor;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ITypeConverter getTypeConverter() {
        return typeConverter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TypedVariable<T> getSelf() {
        return (TypedVariable<T>) applicationContext.getBean(this.getClass());
    }
}