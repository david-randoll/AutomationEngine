package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.variables.IBaseVariable;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.core.variables.VariableContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple variable implementation for testing.
 * Returns a configured value.
 */
@Getter
public class SimpleVariable implements IBaseVariable, IVariable {
    private final String name;
    private final Object value;
    private int resolveCount = 0;
    private final List<EventContext> resolvedContexts = new ArrayList<>();

    public SimpleVariable(String name) {
        this.name = name;
        this.value = "defaultValue";
    }

    public SimpleVariable(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public void resolve(EventContext eventContext) {
        resolveCount++;
        resolvedContexts.add(eventContext);
        eventContext.addMetadata(name, value);
    }

    @Override
    public void resolve(EventContext context, VariableContext variableContext) {
        resolve(context);
    }

    @Override
    public Class<?> getContextType() {
        return VariableContext.class;
    }

    public void reset() {
        resolveCount = 0;
        resolvedContexts.clear();
    }
}