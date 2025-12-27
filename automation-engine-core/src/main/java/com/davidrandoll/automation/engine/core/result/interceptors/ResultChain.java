package com.davidrandoll.automation.engine.core.result.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ResultChain implements IResultChain {
    private final IResultChain delegate;
    private final IResult result;

    @Override
    public Class<?> getContextType() {
        return result.getContextType();
    }

    @Override
    public Object getExecutionSummary(EventContext context, ResultContext resultContext) {
        return delegate.getExecutionSummary(context, resultContext);
    }

    @Override
    public boolean autoEvaluateExpression() {
        return result.autoEvaluateExpression();
    }
}