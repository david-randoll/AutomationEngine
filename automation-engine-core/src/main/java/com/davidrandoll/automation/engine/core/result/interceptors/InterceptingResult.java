package com.davidrandoll.automation.engine.core.result.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.result.ResultContext;

import java.util.List;
import java.util.Optional;

public class InterceptingResult implements IResult {
    private final IResult delegate;
    private final List<IResultInterceptor> interceptors;

    public InterceptingResult(IResult delegate, List<IResultInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = Optional.ofNullable(interceptors).orElse(List.of());
    }


    @Override
    public Object getExecutionSummary(EventContext eventContext, ResultContext resultContext) {
        IResult current = delegate;

        // Wrap each interceptor backwards
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            IResultInterceptor interceptor = interceptors.get(i);
            IResult next = current;

            current = (ec, rc) -> interceptor.intercept(ec, rc, next);
        }

        return current.getExecutionSummary(eventContext, resultContext);
    }
}