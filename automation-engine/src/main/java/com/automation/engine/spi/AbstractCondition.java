package com.automation.engine.spi;

import com.automation.engine.core.conditions.BaseAbstractCondition;
import com.automation.engine.core.conditions.IConditionContext;
import com.automation.engine.core.utils.ITypeConverter;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCondition<T extends IConditionContext> extends BaseAbstractCondition<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Override
    protected ITypeConverter getTypeConverter() {
        return typeConverter;
    }
}
