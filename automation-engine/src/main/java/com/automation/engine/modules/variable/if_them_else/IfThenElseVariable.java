package com.automation.engine.modules.variable.if_them_else;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import com.automation.engine.spi.PluggableVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("ifThenElseVariable")
@RequiredArgsConstructor
public class IfThenElseVariable extends PluggableVariable<IfThenElseVariableContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void resolve(EventContext eventContext, IfThenElseVariableContext variableContext) {
        if (!ObjectUtils.isEmpty(variableContext.getIfConditions())) {
            boolean isSatisfied = resolver.allConditionsSatisfied(eventContext, variableContext.getIfConditions());
            if (isSatisfied) {
                resolver.resolveVariables(eventContext, variableContext.getThenVariables());
                return;
            }
        }

        // check the ifs conditions
        if (!ObjectUtils.isEmpty(variableContext.getIfThenBlocks())) {
            for (var ifBlock : variableContext.getIfThenBlocks()) {
                var isIfsSatisfied = resolver.allConditionsSatisfied(eventContext, ifBlock.getIfConditions());
                if (isIfsSatisfied) {
                    resolver.resolveVariables(eventContext, ifBlock.getThenVariables());
                    return;
                }
            }
        }

        // execute else actions
        resolver.resolveVariables(eventContext, variableContext.getElseVariable());
    }
}