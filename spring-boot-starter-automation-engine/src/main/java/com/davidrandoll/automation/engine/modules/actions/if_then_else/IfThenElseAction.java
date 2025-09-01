package com.davidrandoll.automation.engine.modules.actions.if_then_else;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class IfThenElseAction extends PluggableAction<IfThenElseActionContext> {

    @Override
    public void doExecute(EventContext ec, IfThenElseActionContext ac) {
        if (!ObjectUtils.isEmpty(ac.getIfConditions())) {
            boolean isSatisfied = allConditionsSatisfied(ec, ac.getIfConditions());
            if (isSatisfied) {
                executeActions(ec, ac.getThenActions());
                return;
            }
        }

        // check the ifs conditions
        if (!ObjectUtils.isEmpty(ac.getIfThenBlocks())) {
            for (var ifBlock : ac.getIfThenBlocks()) {
                var isIfsSatisfied = allConditionsSatisfied(ec, ifBlock.getIfConditions());
                if (isIfsSatisfied) {
                    executeActions(ec, ifBlock.getThenActions());
                    return;
                }
            }
        }

        // execute else actions
        executeActions(ec, ac.getElseActions());
    }
}