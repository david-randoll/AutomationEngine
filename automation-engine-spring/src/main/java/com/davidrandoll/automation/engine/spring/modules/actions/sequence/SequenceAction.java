package com.davidrandoll.automation.engine.spring.modules.actions.sequence;


import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

/**
 * A block action that executes a sequence of child actions.
 * <p>
 * With the virtual stack machine architecture, this action simply invokes
 * its child actions and lets the engine handle all state management.
 */
@Slf4j
@RequiredArgsConstructor
public class SequenceAction extends PluggableAction<SequenceActionContext> {

    @Override
    public ActionResult executeWithResult(EventContext ec, SequenceActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getActions())) {
            return ActionResult.continueExecution();
        }
        return ActionResult.invoke(createActions(ec, ac.getActions()));
    }

    @Override
    public void doExecute(EventContext ec, SequenceActionContext ac) {
        // No-op, using executeWithResult instead
    }
}
