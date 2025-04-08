package com.automation.engine.modules.actions.sequence;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("sequenceAction")
@RequiredArgsConstructor
public class SequenceAction extends PluggableAction<SequenceActionContext> {

    @Override
    public void execute(EventContext eventContext, SequenceActionContext actionContext) {
        if (ObjectUtils.isEmpty(actionContext.getActions())) return;
        resolver.executeActions(eventContext, actionContext.getActions());
    }
}
