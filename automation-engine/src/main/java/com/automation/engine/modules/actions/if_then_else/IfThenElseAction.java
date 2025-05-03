package com.automation.engine.modules.actions.if_then_else;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("ifThenElseAction")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "ifThenElseAction", ignored = IfThenElseAction.class)
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