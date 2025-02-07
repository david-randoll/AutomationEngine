package com.automation.engine.modules.action_building_block.variable;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;


@Slf4j
@Component("variableAction")
@RequiredArgsConstructor
public class VariableAction extends AbstractAction<VariableActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(Event event, VariableActionContext context) {
        log.info("Executing variable action");
        if (ObjectUtils.isEmpty(context.getVariables())) return;
        event.addVariables(context.getVariables());
    }
}