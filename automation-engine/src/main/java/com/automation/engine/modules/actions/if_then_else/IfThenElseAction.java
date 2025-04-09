package com.automation.engine.modules.actions.if_then_else;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("ifThenElseAction")
@RequiredArgsConstructor
public class IfThenElseAction extends PluggableAction<IfThenElseActionContext> {

    @Override
    public void execute(EventContext eventContext, IfThenElseActionContext actionContext) {
        if (!ObjectUtils.isEmpty(actionContext.getIfConditions())) {
            boolean isSatisfied = allConditionsSatisfied(eventContext, actionContext.getIfConditions());
            if (isSatisfied) {
                executeActions(eventContext, actionContext.getThenActions());
                return;
            }
        }

        // check the ifs conditions
        if (!ObjectUtils.isEmpty(actionContext.getIfThenBlocks())) {
            for (var ifBlock : actionContext.getIfThenBlocks()) {
                var isIfsSatisfied = allConditionsSatisfied(eventContext, ifBlock.getIfConditions());
                if (isIfsSatisfied) {
                    executeActions(eventContext, ifBlock.getThenActions());
                    return;
                }
            }
        }

        // execute else actions
        executeActions(eventContext, actionContext.getElseActions());
    }
}