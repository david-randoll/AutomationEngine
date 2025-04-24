package com.automation.engine.modules.actions.stop;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.actions.exceptions.StopAutomationException;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component("stopAction")
@RequiredArgsConstructor
public class StopAction extends PluggableAction<StopActionContext> {

    @Override
    public void execute(EventContext ec, StopActionContext ac) {
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