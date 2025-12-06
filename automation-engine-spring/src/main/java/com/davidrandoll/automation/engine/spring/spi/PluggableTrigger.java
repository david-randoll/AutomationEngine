package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.automation.engine.creator.AutomationProcessor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PluggableTrigger<T extends ITriggerContext> implements TypedTrigger<T> {
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
