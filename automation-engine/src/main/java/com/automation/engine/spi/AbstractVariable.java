package com.automation.engine.spi;

import com.automation.engine.core.utils.ITypeConverter;
import com.automation.engine.core.variables.BaseAbstractVariable;
import com.automation.engine.core.variables.IVariableContext;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractVariable<T extends IVariableContext> extends BaseAbstractVariable<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Override
    protected ITypeConverter getTypeConverter() {
        return typeConverter;
    }
}
