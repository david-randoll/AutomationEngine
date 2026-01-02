package com.davidrandoll.automation.engine.spring.modules.actions.if_then_else;

import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * A block action that evaluates conditions and invokes the appropriate branch.
 * <p>
 * With the virtual stack machine architecture, this action simply decides which
 * branch to take and returns {@link ActionResult#invoke(List)} with the chosen
 * actions. The execution engine handles all state management for pause/resume.
 */
@RequiredArgsConstructor
public class IfThenElseAction extends PluggableAction<IfThenElseActionContext> {
    @Override
    public ActionResult executeWithResult(EventContext ec, IfThenElseActionContext ac) {
        // Check main if conditions first
        if (!ObjectUtils.isEmpty(ac.getIfConditions())) {
            boolean isSatisfied = allConditionsSatisfied(ec, ac.getIfConditions());
            if (isSatisfied) {
                return invokeBranch(ec, ac.getThenActions());
            }
        }

        // Check the else-if blocks
        if (!ObjectUtils.isEmpty(ac.getIfThenBlocks())) {
            for (var ifBlock : ac.getIfThenBlocks()) {
                var isIfsSatisfied = allConditionsSatisfied(ec, ifBlock.getIfConditions());
                if (isIfsSatisfied) {
                    return invokeBranch(ec, ifBlock.getThenActions());
                }
            }
        }

        // Execute else actions (may be empty/null)
        return invokeBranch(ec, ac.getElseActions());
    }

    @Override
    public void doExecute(EventContext ec, IfThenElseActionContext ac) {
        // No-op, using executeWithResult instead
    }

    /**
     * Invokes a branch by creating actions and returning INVOKE result.
     * The engine will push these onto the execution stack.
     */
    private ActionResult invokeBranch(EventContext ec, List<ActionDefinition> actions) {
        if (ObjectUtils.isEmpty(actions)) {
            return ActionResult.continueExecution();
        }
        return ActionResult.invoke(createActions(ec, actions));
    }
}