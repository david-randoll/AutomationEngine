package com.automation.engine.core.actions;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.events.Event;

@FunctionalInterface
public interface IBaseAction {
    void execute(Event context) throws StopActionSequenceException;

    default void execute(Event context, ActionContext actionContext) throws StopActionSequenceException {
        execute(context);
    }
}