package com.davidrandoll.automation.engine.orchestrator.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;

public interface IAutomationExecutionInterceptor {
    AutomationResult intercept(Automation automation, EventContext context, IAutomationExecutionChain chain);
}