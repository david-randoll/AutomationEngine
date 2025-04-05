package com.automation.engine.spi;

import com.automation.engine.core.actions.BaseAbstractAction;
import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.core.utils.ITypeConverter;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAction<T extends IActionContext> extends BaseAbstractAction<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Override
    protected ITypeConverter getTypeConverter() {
        return typeConverter;
    }
}
