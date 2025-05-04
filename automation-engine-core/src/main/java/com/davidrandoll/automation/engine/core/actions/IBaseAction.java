package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopAutomationException;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseAction {
    void execute(EventContext context) throws StopActionSequenceException, StopAutomationException;
}