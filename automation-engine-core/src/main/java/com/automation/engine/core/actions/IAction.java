package com.automation.engine.core.actions;

import com.automation.engine.core.events.EventContext;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface IAction extends IBaseAction {
    @Override
    default void execute(EventContext context) {
        // used by FunctionalInterface to execute the execute method with ActionContext
    }

    @Nullable
    default Class<?> getContextType() {
        return null;
    }

    @Override
    void execute(EventContext context, ActionContext actionContext);
}