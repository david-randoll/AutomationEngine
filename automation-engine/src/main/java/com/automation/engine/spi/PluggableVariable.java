package com.automation.engine.spi;

import com.automation.engine.core.utils.ITypeConverter;
import com.automation.engine.core.variables.IVariableContext;
import com.automation.engine.factory.AutomationProcessor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PluggableVariable<T extends IVariableContext> implements TypedVariable<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Autowired
    @Delegate
    protected AutomationProcessor processor;

    @Override
    public ITypeConverter getTypeConverter() {
        return typeConverter;
    }
}
