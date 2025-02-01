package com.automation.engine.modules.action_building_block.stop_on_condition;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component("stopActionSequenceAction")
@RequiredArgsConstructor
public class StopActionSequenceAction extends AbstractAction<StopActionSequenceActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(Event eventContext, StopActionSequenceActionContext actionContext) throws StopActionSequenceException {
        if (ObjectUtils.isEmpty(actionContext.getCondition())) return;
        var isSatisfied = resolver.allConditionsSatisfied(eventContext, List.of(actionContext.getCondition()));
        if (isSatisfied) throw new StopActionSequenceException();
    }
}