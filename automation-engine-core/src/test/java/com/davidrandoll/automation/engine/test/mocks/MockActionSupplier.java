package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.creator.actions.IActionSupplier;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of IActionSupplier for testing.
 */
@Getter
public class MockActionSupplier implements IActionSupplier {
    private final Map<String, IAction> actions = new HashMap<>();

    public void register(String name, IAction action) {
        actions.put(name, action);
    }

    @Override
    public IAction getAction(String actionName) {
        return actions.get(actionName);
    }
}
