package com.automation.engine.modules.actions.if_then_else;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("ifThenElseAction")
@RequiredArgsConstructor
public class IfThenElseAction extends AbstractAction<IfThenElseActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(EventContext eventContext, IfThenElseActionContext actionContext) {
        if (!ObjectUtils.isEmpty(actionContext.getIfConditions())) {
            boolean isSatisfied = resolver.allConditionsSatisfied(eventContext, actionContext.getIfConditions());
            if (isSatisfied) {
                resolver.executeActions(eventContext, actionContext.getThenActions());
                return;
            }
        }

        // check the ifs conditions
        if (!ObjectUtils.isEmpty(actionContext.getIfThenBlocks())) {
            for (var ifBlock : actionContext.getIfThenBlocks()) {
                var isIfsSatisfied = resolver.allConditionsSatisfied(eventContext, ifBlock.getIfConditions());
                if (isIfsSatisfied) {
                    resolver.executeActions(eventContext, ifBlock.getThenActions());
                    return;
                }
            }
        }

        // execute else actions
        resolver.executeActions(eventContext, actionContext.getElseActions());
    }
}