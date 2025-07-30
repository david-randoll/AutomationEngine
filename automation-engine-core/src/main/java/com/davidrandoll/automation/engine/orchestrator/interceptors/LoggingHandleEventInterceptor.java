package com.davidrandoll.automation.engine.orchestrator.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingHandleEventInterceptor implements IAutomationHandleEventInterceptor {
    @Override
    public void intercept(EventContext eventContext, IAutomationHandleEventChain chain) {
        var eventName = eventContext.getEvent().getClass().getSimpleName();
        log.debug("Handling event: {}", eventName);
        chain.proceed(eventContext);
        log.debug("Finished handling event: {}", eventName);
    }
}