package com.automation.engine.core.actions;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.actions.exceptions.StopAutomationException;
import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IAction {
    default Class<?> getContextType() {
        return null;
    }

    void execute(EventContext context, ActionContext actionContext) throws StopActionSequenceException, StopAutomationException;
}