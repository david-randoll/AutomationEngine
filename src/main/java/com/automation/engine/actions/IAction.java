package com.automation.engine.actions;

import com.automation.engine.events.EventContext;

public interface IAction {
    void execute(EventContext context);
}