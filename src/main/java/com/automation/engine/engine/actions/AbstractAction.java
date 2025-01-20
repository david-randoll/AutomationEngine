package com.automation.engine.engine.actions;

import com.automation.engine.engine.events.EventContext;

public abstract class AbstractAction implements IAction {
    @Override
    public void execute(EventContext context) {
        // used by FunctionalInterface to execute the execute method with ActionContext
    }

    public abstract void execute(EventContext context, ActionContext actionContext);
}