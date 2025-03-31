package com.automation.engine.modules.actions.stop;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.actions.exceptions.StopAutomationException;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component("stopAction")
@RequiredArgsConstructor
public class StopAction extends AbstractAction<StopActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(Event event, StopActionContext context) {
        if (ObjectUtils.isEmpty(context.getCondition())) return;
        var isSatisfied = resolver.allConditionsSatisfied(event, List.of(context.getCondition()));
        if (!isSatisfied) return;
        if (context.hasStopMessage()) {
            if (context.isStopAutomation()) throw new StopAutomationException(context.getStopMessage());
            if (context.isStopActionSequence()) throw new StopActionSequenceException(context.getStopMessage());
        } else {
            if (context.isStopAutomation()) throw new StopAutomationException();
            if (context.isStopActionSequence()) throw new StopActionSequenceException();
        }
    }
}