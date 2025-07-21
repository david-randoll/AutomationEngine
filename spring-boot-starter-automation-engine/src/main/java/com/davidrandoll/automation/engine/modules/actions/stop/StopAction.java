package com.davidrandoll.automation.engine.modules.actions.stop;


import com.davidrandoll.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopAutomationException;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component("stopAction")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "stopAction", ignored = StopAction.class)
public class StopAction extends PluggableAction<StopActionContext> {

    @Override
    public void doExecute(EventContext ec, StopActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getCondition())) return;
        var isSatisfied = processor.allConditionsSatisfied(ec, List.of(ac.getCondition()));
        if (!isSatisfied) return;
        if (ac.hasStopMessage()) {
            if (ac.isStopAutomation()) throw new StopAutomationException(ac.getStopMessage());
            if (ac.isStopActionSequence())
                throw new StopActionSequenceException(ac.getStopMessage());
        } else {
            if (ac.isStopAutomation()) throw new StopAutomationException();
            if (ac.isStopActionSequence()) throw new StopActionSequenceException();
        }
    }
}