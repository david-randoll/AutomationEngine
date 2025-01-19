package com.automation.engine.actions;

import com.automation.engine.events.EventContext;

@FunctionalInterface
public interface IAction {
    void execute(EventContext context);

    default void execute(EventContext context, ActionContext actionContext) {
        execute(context);
    }
}