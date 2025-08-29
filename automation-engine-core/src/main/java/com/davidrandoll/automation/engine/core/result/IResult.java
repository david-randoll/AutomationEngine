package com.davidrandoll.automation.engine.core.result;

import com.davidrandoll.automation.engine.core.IBlock;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IResult extends IBlock {
    Object getExecutionSummary(EventContext context, ResultContext resultContext);
}
