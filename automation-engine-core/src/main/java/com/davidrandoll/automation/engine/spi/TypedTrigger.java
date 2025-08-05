package com.davidrandoll.automation.engine.spi;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

import java.util.Map;

public interface TypedTrigger<T extends ITriggerContext> extends ITrigger {
    ITypeConverter getTypeConverter();

    IExpressionResolver getExpressionResolver();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default boolean isTriggered(EventContext eventContext, TriggerContext triggerContext) {
        Map<String, Object> evaluatedContextData = triggerContext.getData();
        if (autoEvaluateExpression()) {
            evaluatedContextData = getExpressionResolver().resolve(eventContext, triggerContext.getData());
        }

        T data = getTypeConverter().convert(evaluatedContextData, getContextType());
        return isTriggered(eventContext, data);
    }

    boolean isTriggered(EventContext ec, T tc);

    default boolean autoEvaluateExpression() {
        return true;
    }
}