package com.automation.engine.spi;

import com.automation.engine.core.triggers.BaseAbstractTrigger;
import com.automation.engine.core.triggers.ITriggerContext;
import com.automation.engine.core.utils.ITypeConverter;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractTrigger<T extends ITriggerContext> extends BaseAbstractTrigger<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Override
    protected ITypeConverter getTypeConverter() {
        return typeConverter;
    }
}
