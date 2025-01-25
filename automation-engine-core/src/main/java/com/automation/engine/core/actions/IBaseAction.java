package com.automation.engine.core.actions;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseAction {
    void execute(EventContext context);

    default void execute(EventContext context, ActionContext actionContext) {
        execute(context);
    }
}