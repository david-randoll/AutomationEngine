package com.automation.engine.modules.action_building_block.if_then_else;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("ifThenElseAction")
@RequiredArgsConstructor
public class IfThenElseAction extends AbstractAction<IfThenElseActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(Event event, IfThenElseActionContext context) {
        if (!ObjectUtils.isEmpty(context.getIfConditions())) {
            boolean isSatisfied = resolver.allConditionsSatisfied(event, context.getIfConditions());
            if (isSatisfied) {
                resolver.executeActions(event, context.getThenActions());
                return;
            }
        }

        // check the ifs conditions
        if (!ObjectUtils.isEmpty(context.getIfThenBlocks())) {
            for (var ifBlock : context.getIfThenBlocks()) {
                var isIfsSatisfied = resolver.allConditionsSatisfied(event, ifBlock.getIfConditions());
                if (isIfsSatisfied) {
                    resolver.executeActions(event, ifBlock.getThenActions());
                    return;
                }
            }
        }

        // execute else actions
        resolver.executeActions(event, context.getElseActions());
    }
}