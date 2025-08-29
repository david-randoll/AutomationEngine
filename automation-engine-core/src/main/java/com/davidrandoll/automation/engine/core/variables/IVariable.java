package com.davidrandoll.automation.engine.core.variables;

import com.davidrandoll.automation.engine.core.IBlock;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IVariable extends IBlock {
    void resolve(EventContext context, VariableContext variableContext);
}