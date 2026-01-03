package com.davidrandoll.automation.engine.spring.modules.actions.stop;


import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopAutomationException;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;

@RequiredArgsConstructor
public class StopAction extends PluggableAction<StopActionContext> {

    @Override
    public ActionResult executeWithResult(EventContext ec, StopActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getCondition()))
            return ActionResult.continueExecution();
        var isSatisfied = processor.allConditionsSatisfied(ec, List.of(ac.getCondition()));
        if (!isSatisfied) return ActionResult.continueExecution();

        if (ac.hasStopMessage()) {
            if (ac.isStopAutomation()) throw new StopAutomationException(ac.getStopMessage());
            if (ac.isStopActionSequence())
                throw new StopActionSequenceException(ac.getStopMessage());
        } else {
            if (ac.isStopAutomation()) throw new StopAutomationException();
            if (ac.isStopActionSequence()) throw new StopActionSequenceException();
        }
        return ActionResult.stopExecution();
    }

    @Override
    public void doExecute(EventContext ec, StopActionContext ac) {
        // No-op, using executeWithResult instead
    }
}