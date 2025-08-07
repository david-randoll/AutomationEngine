package com.davidrandoll.automation.engine.core.result;

import com.davidrandoll.automation.engine.core.IModule;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IResult extends IModule {
    Object getExecutionSummary(EventContext context, ResultContext resultContext);
}
