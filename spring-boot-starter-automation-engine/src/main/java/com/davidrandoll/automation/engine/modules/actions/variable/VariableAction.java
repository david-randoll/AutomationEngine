package com.davidrandoll.automation.engine.modules.actions.variable;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;


@Slf4j
@RequiredArgsConstructor
public class VariableAction extends PluggableAction<VariableActionContext> {

    @Override
    public void doExecute(EventContext ec, VariableActionContext ac) {
        log.info("Executing variable action");
        if (ObjectUtils.isEmpty(ac.getVariables())) return;
        ec.addMetadata(ac.getVariables());
    }
}