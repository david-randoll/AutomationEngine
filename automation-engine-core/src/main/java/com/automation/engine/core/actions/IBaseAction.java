package com.automation.engine.core.actions;

import com.automation.engine.core.events.Event;

@FunctionalInterface
public interface IBaseAction {
    void execute(Event context);

    default void execute(Event context, ActionContext actionContext) {
        execute(context);
    }
}