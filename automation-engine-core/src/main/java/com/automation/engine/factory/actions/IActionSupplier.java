package com.automation.engine.factory.actions;

import com.automation.engine.core.actions.IAction;

public interface IActionSupplier {
    IAction getAction(String name);
}