package com.automation.engine.spi;

import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.core.utils.ITypeConverter;
import com.automation.engine.factory.AutomationResolver;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PluggableAction<T extends IActionContext> implements TypedAction<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Autowired
    protected AutomationResolver resolver;

    @Override
    public ITypeConverter getTypeConverter() {
        return typeConverter;
    }
}
