package com.automation.engine.modules.action_building_block.stop_action_sequence;

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
    public void execute(Event event, StopActionSequenceActionContext context) throws StopActionSequenceException {
        if (ObjectUtils.isEmpty(context.getCondition())) return;
        var isSatisfied = resolver.allConditionsSatisfied(event, List.of(context.getCondition()));
        if (isSatisfied && context.isStopIfSatisfied()) throw new StopActionSequenceException();
    }
}