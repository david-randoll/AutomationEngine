package com.automation.engine.spi;

import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.creator.AutomationProcessor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PluggableAction<T extends IActionContext> implements TypedAction<T> {
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
