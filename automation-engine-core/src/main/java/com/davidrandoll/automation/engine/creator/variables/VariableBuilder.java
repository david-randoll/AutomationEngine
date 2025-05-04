package com.davidrandoll.automation.engine.creator.variables;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.variables.BaseVariableList;
import com.davidrandoll.automation.engine.core.variables.IBaseVariable;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.core.variables.VariableContext;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.davidrandoll.automation.engine.core.variables.interceptors.InterceptingVariable;
import com.davidrandoll.automation.engine.creator.actions.ActionNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class VariableBuilder {
    private final IVariableSupplier supplier;
    private final List<IVariableInterceptor> variableInterceptors;

    public BaseVariableList resolve(List<Variable> variables) {
        var result = new BaseVariableList();

        if (isNull(variables)) return result;

        for (Variable variable : variables) {
            IBaseVariable newVariableInstance = buildVariable(variable);
            result.add(newVariableInstance);
        }

        return result;
    }

    private IBaseVariable buildVariable(Variable variable) {
        IVariable variableInstance = Optional.ofNullable(supplier.getVariable(variable.getVariable()))
                .orElseThrow(() -> new ActionNotFoundException(variable.getVariable()));

        var interceptingVariable = new InterceptingVariable(variableInstance, variableInterceptors);
        var variableContext = new VariableContext(variable.getParams());

        return event -> interceptingVariable.resolve(event, variableContext);
    }


    public void resolveVariables(EventContext eventContext, List<Variable> variables) {
        BaseVariableList resolvedVariables = resolve(variables);
        resolvedVariables.resolveAll(eventContext);
    }
}