package com.automation.engine.factory.variables;

import com.automation.engine.core.variables.IVariable;

public interface IVariableSupplier {
    IVariable getVariable(String name);
}