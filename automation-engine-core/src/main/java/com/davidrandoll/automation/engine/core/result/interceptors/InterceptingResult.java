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
        IResultChain chain = buildChain(0);
        return chain.getExecutionSummary(eventContext, new ResultContext(resultContext));
    }

    private IResultChain buildChain(int index) {
        if (index >= interceptors.size()) {
            return new ResultChain(this.delegate::getExecutionSummary, delegate);
        }

        IResultInterceptor interceptor = interceptors.get(index);
        IResultChain next = buildChain(index + 1);
        return new ResultChain(
                (ec, rc) -> interceptor.intercept(ec, rc, next),
                delegate
        );
    }
}