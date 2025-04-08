package com.automation.engine.spi;

import com.automation.engine.core.utils.ITypeConverter;
import com.automation.engine.core.variables.IVariableContext;
import com.automation.engine.factory.AutomationResolver;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PluggableVariable<T extends IVariableContext> implements TypedVariable<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Autowired
    private AutomationResolver resolver;

    @Override
    public ITypeConverter getTypeConverter() {
        return typeConverter;
    }
}
