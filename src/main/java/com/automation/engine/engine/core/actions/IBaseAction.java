package com.automation.engine.engine.core.actions;

import com.automation.engine.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseAction {
    void execute(EventContext context);

    default void execute(EventContext context, ActionContext actionContext) {
        execute(context);
    }
}