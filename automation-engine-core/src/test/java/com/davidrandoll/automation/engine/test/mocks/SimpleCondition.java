package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.conditions.IBaseCondition;
import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple condition implementation for testing.
 * Allows setting the evaluation result.
 */
@Getter
@Setter
public class SimpleCondition implements IBaseCondition {
    private final String name;
    private boolean evaluationResult = true;
    private int evaluationCount = 0;
    private final List<EventContext> evaluatedContexts = new ArrayList<>();

    public SimpleCondition(String name) {
        this.name = name;
    }

    public SimpleCondition(String name, boolean result) {
        this.name = name;
        this.evaluationResult = result;
    }

    @Override
    public boolean isSatisfied(EventContext eventContext) {
        evaluationCount++;
        evaluatedContexts.add(eventContext);
        return evaluationResult;
    }

    public void reset() {
        evaluationCount = 0;
        evaluatedContexts.clear();
        evaluationResult = true;
    }
}