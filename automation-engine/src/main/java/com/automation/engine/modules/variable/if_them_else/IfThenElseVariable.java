package com.automation.engine.modules.variable.if_them_else;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.variables.AbstractVariable;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("ifThenElseVariable")
@RequiredArgsConstructor
public class IfThenElseVariable extends AbstractVariable<IfThenElseVariableContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void resolve(Event event, IfThenElseVariableContext context) {
        if (!ObjectUtils.isEmpty(context.getIfConditions())) {
            boolean isSatisfied = resolver.allConditionsSatisfied(event, context.getIfConditions());
            if (isSatisfied) {
                resolver.resolveVariables(event, context.getThenVariables());
                return;
            }
        }

        // check the ifs conditions
        if (!ObjectUtils.isEmpty(context.getIfThenBlocks())) {
            for (var ifBlock : context.getIfThenBlocks()) {
                var isIfsSatisfied = resolver.allConditionsSatisfied(event, ifBlock.getIfConditions());
                if (isIfsSatisfied) {
                    resolver.resolveVariables(event, ifBlock.getThenVariables());
                    return;
                }
            }
        }

        // execute else actions
        resolver.resolveVariables(event, context.getElseVariable());
    }
}