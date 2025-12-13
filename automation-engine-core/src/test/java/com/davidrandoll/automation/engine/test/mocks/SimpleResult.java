package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SimpleResult implements IResult {
    private final String name;

    @Override
    public Object getExecutionSummary(EventContext context, ResultContext resultContext) {
        return "Result executed: " + name;
    }
}
