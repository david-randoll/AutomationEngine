package com.davidrandoll.automation.engine.creator.result;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.IBaseResult;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.core.result.interceptors.InterceptingResult;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ResultBuilder {
    private final IResultSupplier supplier;
    private final List<IResultInterceptor> interceptors;

    public IBaseResult resolve(ResultDefinition result) {
        IResult instance = Optional.ofNullable(supplier.getResult(result.getResult()))
                .orElseThrow(() -> new ResultNotFoundException(result.getResult()));

        var interceptingResult = new InterceptingResult(instance, interceptors);
        var resultContext = new ResultContext(result);

        return ec -> interceptingResult.getExecutionSummary(ec, resultContext);
    }

    public Object resolveResult(EventContext eventContext, ResultDefinition result) {
        IBaseResult resolvedResult = resolve(result);
        return resolvedResult.getExecutionSummary(eventContext);
    }
}