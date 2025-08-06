package com.davidrandoll.automation.engine.core.result.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;

@FunctionalInterface
public interface IResultInterceptor {
    Object intercept(EventContext eventContext, ResultContext resultContext, IResultChain chain);
}