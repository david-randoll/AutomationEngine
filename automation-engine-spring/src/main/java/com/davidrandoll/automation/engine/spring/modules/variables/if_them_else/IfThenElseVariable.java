package com.davidrandoll.automation.engine.spring.modules.variables.if_them_else;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class IfThenElseVariable extends PluggableVariable<IfThenElseVariableContext> {

    @Override
    public void resolve(EventContext eventContext, IfThenElseVariableContext variableContext) {
        if (!ObjectUtils.isEmpty(variableContext.getIfConditions())) {
            boolean isSatisfied = allConditionsSatisfied(eventContext, variableContext.getIfConditions());
            if (isSatisfied) {
                resolveVariables(eventContext, variableContext.getThenVariables());
                return;
            }
        }

        // check the ifs conditions
        if (!ObjectUtils.isEmpty(variableContext.getIfThenBlocks())) {
            for (var ifBlock : variableContext.getIfThenBlocks()) {
                var isIfsSatisfied = allConditionsSatisfied(eventContext, ifBlock.getIfConditions());
                if (isIfsSatisfied) {
                    resolveVariables(eventContext, ifBlock.getThenVariables());
                    return;
                }
            }
        }

        // execute else actions
        resolveVariables(eventContext, variableContext.getElseVariable());
    }
}