package com.automation.engine.core.actions;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.actions.exceptions.StopAutomationException;
import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseAction {
    void execute(EventContext context) throws StopActionSequenceException, StopAutomationException;
}