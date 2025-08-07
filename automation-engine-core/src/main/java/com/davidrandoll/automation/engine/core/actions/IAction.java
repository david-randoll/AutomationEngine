package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.core.IModule;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopAutomationException;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IAction extends IModule {
    void execute(EventContext context, ActionContext actionContext) throws StopActionSequenceException, StopAutomationException;
}