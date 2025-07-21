package com.davidrandoll.automation.engine.modules.variables.if_them_else;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("ifThenElseVariable")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "ifThenElseVariable", ignored = IfThenElseVariable.class)
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