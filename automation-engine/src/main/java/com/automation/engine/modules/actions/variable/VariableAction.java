package com.automation.engine.modules.actions.variable;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;


@Slf4j
@Component("variableAction")
@RequiredArgsConstructor
public class VariableAction extends PluggableAction<VariableActionContext> {

    @Override
    public void execute(EventContext ec, VariableActionContext ac) {
        log.info("Executing variable action");
        if (ObjectUtils.isEmpty(ac.getVariables())) return;
        ec.addMetadata(ac.getVariables());
    }
}