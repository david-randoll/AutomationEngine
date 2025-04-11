package com.automation.engine.creator.variables;

import com.automation.engine.core.variables.IVariable;

public interface IVariableSupplier {
    IVariable getVariable(String name);
}