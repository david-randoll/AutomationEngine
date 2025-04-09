package com.automation.engine.spi;

import com.automation.engine.core.triggers.ITriggerContext;
import com.automation.engine.core.utils.ITypeConverter;
import com.automation.engine.factory.AutomationProcessor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PluggableTrigger<T extends ITriggerContext> implements TypedTrigger<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Autowired
    @Delegate
    private AutomationProcessor processor;

    @Override
    public ITypeConverter getTypeConverter() {
        return typeConverter;
    }
}
