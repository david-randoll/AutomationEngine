package com.automation.engine.core.actions;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.events.Event;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface IAction extends IBaseAction {
    @Override
    default void execute(Event context) throws StopActionSequenceException {
        // used by FunctionalInterface to execute the execute method with ActionContext
    }

    @Nullable
    default Class<?> getContextType() {
        return null;
    }

    @Override
    void execute(Event context, ActionContext actionContext) throws StopActionSequenceException;
}