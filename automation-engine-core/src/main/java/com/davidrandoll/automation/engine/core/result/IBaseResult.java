package com.davidrandoll.automation.engine.core.result;

import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseResult {
    Object getExecutionSummary(EventContext context);
}