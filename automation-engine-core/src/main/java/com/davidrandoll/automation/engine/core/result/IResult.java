package com.davidrandoll.automation.engine.core.result;

import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IResult {
    default Class<?> getContextType() {
        return null;
    }

    Object getExecutionSummary(EventContext context, ResultContext resultContext);

    default boolean autoEvaluateExpression() {
        return true;
    }
}
