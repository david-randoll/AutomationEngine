package com.davidrandoll.automation.engine.creator.actions;

import com.davidrandoll.automation.engine.core.actions.IAction;

public interface IActionSupplier {
    IAction getAction(String name);
}