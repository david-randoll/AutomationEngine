package com.davidrandoll.automation.engine.spring.modules.actions.if_then_else;

import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;

@RequiredArgsConstructor
public class IfThenElseAction extends PluggableAction<IfThenElseActionContext> {

    @Override
    public ActionResult executeWithResult(EventContext ec, IfThenElseActionContext ac) {
        Integer branchIndex = null;
        if (!ec.getExecutionStack().isEmpty()) {
            branchIndex = ec.getExecutionStack().pop();
        }

        if (branchIndex == null) {
            if (!ObjectUtils.isEmpty(ac.getIfConditions())) {
                boolean isSatisfied = allConditionsSatisfied(ec, ac.getIfConditions());
                if (isSatisfied) {
                    return executeBranch(ec, 0, ac.getThenActions());
                }
            }

            // check the ifs conditions
            if (!ObjectUtils.isEmpty(ac.getIfThenBlocks())) {
                for (int i = 0; i < ac.getIfThenBlocks().size(); i++) {
                    var ifBlock = ac.getIfThenBlocks().get(i);
                    var isIfsSatisfied = allConditionsSatisfied(ec, ifBlock.getIfConditions());
                    if (isIfsSatisfied) {
                        return executeBranch(ec, i + 1, ifBlock.getThenActions());
                    }
                }
            }

            // execute else actions
            return executeBranch(ec, -1, ac.getElseActions());
        } else {
            // Resuming
            if (branchIndex == 0) {
                return executeBranch(ec, 0, ac.getThenActions());
            } else if (branchIndex == -1) {
                return executeBranch(ec, -1, ac.getElseActions());
            } else {
                var ifBlock = ac.getIfThenBlocks().get(branchIndex - 1);
                return executeBranch(ec, branchIndex, ifBlock.getThenActions());
            }
        }
    }

    @Override
    public void doExecute(EventContext ec, IfThenElseActionContext ac) {
        // No-op, using executeWithResult instead
    }

    private ActionResult executeBranch(EventContext ec, int branchIndex, List<ActionDefinition> actions) {
        ActionResult result = executeActions(ec, actions);
        if (result == ActionResult.PAUSE) {
            ec.getExecutionStack().push(branchIndex);
        }
        return result;
    }
}