package com.automation.engine.core.actions;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.actions.exceptions.StopAutomationException;
import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IAction extends IBaseAction {
    @Override
    default void execute(EventContext context) throws StopActionSequenceException, StopAutomationException {
        // used by FunctionalInterface to execute the execute method with ActionContext
    }

    default Class<?> getContextType() {
        return null;
    }

    @Override
    void execute(EventContext context, ActionContext actionContext) throws StopActionSequenceException, StopAutomationException;
}