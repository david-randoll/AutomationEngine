package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.creator.variables.IVariableSupplier;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of IVariableSupplier for testing.
 */
@Getter
public class MockVariableSupplier implements IVariableSupplier {
    private final Map<String, IVariable> variables = new HashMap<>();

    public void register(String name, IVariable variable) {
        variables.put(name, variable);
    }

    @Override
    public IVariable getVariable(String variableName) {
        return variables.get(variableName);
    }
}
