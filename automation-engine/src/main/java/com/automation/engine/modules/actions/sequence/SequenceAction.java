package com.automation.engine.modules.actions.sequence;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("sequenceAction")
@RequiredArgsConstructor
public class SequenceAction extends AbstractAction<SequenceActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(Event event, SequenceActionContext context) {
        if (ObjectUtils.isEmpty(context.getActions())) return;
        resolver.executeActions(event, context.getActions());
    }
}
