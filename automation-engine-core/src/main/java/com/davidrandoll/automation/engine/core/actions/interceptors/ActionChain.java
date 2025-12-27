package com.davidrandoll.automation.engine.core.actions.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopAutomationException;
import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ActionChain implements IActionChain {
    private final IActionChain delegate;
    private final IAction action;

    @Override
    public Class<?> getContextType() {
        return action.getContextType();
    }

    @Override
    public void execute(EventContext context, ActionContext actionContext) throws StopActionSequenceException, StopAutomationException {
        delegate.execute(context, actionContext);
    }

    @Override
    public boolean autoEvaluateExpression() {
        return action.autoEvaluateExpression();
    }
}
