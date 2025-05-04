package com.davidrandoll.automation.engine.creator.variables;

import com.davidrandoll.automation.engine.core.variables.IVariable;

public interface IVariableSupplier {
    IVariable getVariable(String name);
}