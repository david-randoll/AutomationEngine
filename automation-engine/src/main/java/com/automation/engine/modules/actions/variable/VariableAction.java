package com.automation.engine.modules.actions.variable;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.AbstractAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;


@Slf4j
@Component("variableAction")
@RequiredArgsConstructor
public class VariableAction extends AbstractAction<VariableActionContext> {

    @Override
    public void execute(EventContext eventContext, VariableActionContext actionContext) {
        log.info("Executing variable action");
        if (ObjectUtils.isEmpty(actionContext.getVariables())) return;
        eventContext.addMetadata(actionContext.getVariables());
    }
}