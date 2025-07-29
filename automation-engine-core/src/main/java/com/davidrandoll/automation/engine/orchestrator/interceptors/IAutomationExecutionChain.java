package com.davidrandoll.automation.engine.orchestrator.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;

@FunctionalInterface
public interface IAutomationExecutionChain {
    AutomationResult proceed(Automation automation, EventContext context);
}