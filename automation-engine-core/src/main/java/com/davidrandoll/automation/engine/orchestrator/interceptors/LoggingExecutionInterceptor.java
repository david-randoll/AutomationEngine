package com.davidrandoll.automation.engine.orchestrator.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingExecutionInterceptor implements IAutomationExecutionInterceptor {
    @Override
    public AutomationResult intercept(Automation automation, EventContext context, IAutomationExecutionChain chain) {
        log.debug("Starting execution of automation: {}", automation.getAlias());
        var result = chain.proceed(automation, context);
        log.debug("Finished execution of automation: {}", automation.getAlias());
        return result;
    }
}