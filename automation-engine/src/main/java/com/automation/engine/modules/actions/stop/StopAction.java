package com.automation.engine.modules.actions.stop;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.actions.exceptions.StopAutomationException;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import com.automation.engine.spi.AbstractAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component("stopAction")
@RequiredArgsConstructor
public class StopAction extends AbstractAction<StopActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(EventContext eventContext, StopActionContext actionContext) {
        if (ObjectUtils.isEmpty(actionContext.getCondition())) return;
        var isSatisfied = resolver.allConditionsSatisfied(eventContext, List.of(actionContext.getCondition()));
        if (!isSatisfied) return;
        if (actionContext.hasStopMessage()) {
            if (actionContext.isStopAutomation()) throw new StopAutomationException(actionContext.getStopMessage());
            if (actionContext.isStopActionSequence())
                throw new StopActionSequenceException(actionContext.getStopMessage());
        } else {
            if (actionContext.isStopAutomation()) throw new StopAutomationException();
            if (actionContext.isStopActionSequence()) throw new StopActionSequenceException();
        }
    }
}