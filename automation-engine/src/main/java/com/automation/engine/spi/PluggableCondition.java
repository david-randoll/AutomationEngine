package com.automation.engine.spi;

import com.automation.engine.core.conditions.IConditionContext;
import com.automation.engine.creator.AutomationProcessor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PluggableCondition<T extends IConditionContext> implements TypedCondition<T> {
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
