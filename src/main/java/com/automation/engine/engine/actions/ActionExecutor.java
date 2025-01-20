package com.automation.engine.engine.actions;

import com.automation.engine.engine.events.EventContext;

@FunctionalInterface
public interface ActionExecutor extends IAction {
    @Override
    default void execute(EventContext context) {
        // used by FunctionalInterface to execute the execute method with ActionContext
    }

    void execute(EventContext context, ActionContext actionContext);
}