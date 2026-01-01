package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.actions.IBaseAction;
import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple action implementation for testing.
 * Tracks execution count and can be configured to throw exceptions.
 */
@Getter
public class SimpleAction implements IBaseAction, IAction {
    private final String name;
    private int executionCount = 0;
    private final List<EventContext> executedContexts = new ArrayList<>();
    @Setter
    private RuntimeException exceptionToThrow;

    public SimpleAction(String name) {
        this.name = name;
    }

    @Override
    public ActionResult execute(EventContext eventContext) {
        executionCount++;
        executedContexts.add(eventContext);

        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
        return ActionResult.CONTINUE;
    }

    @Override
    public ActionResult execute(EventContext context, ActionContext actionContext) {
        return execute(context);
    }

    @Override
    public Class<?> getContextType() {
        return ActionContext.class;
    }

    public void reset() {
        executionCount = 0;
        executedContexts.clear();
        exceptionToThrow = null;
    }
}