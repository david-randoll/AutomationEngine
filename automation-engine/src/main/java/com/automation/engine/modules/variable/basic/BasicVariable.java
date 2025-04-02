package com.automation.engine.modules.variable.basic;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.variables.AbstractVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("basicVariable")
@RequiredArgsConstructor
public class BasicVariable extends AbstractVariable<BasicVariableContext> {

    @Override
    public void resolve(EventContext eventContext, BasicVariableContext variableContext) {
        log.info("Resolving basic variable: {}", variableContext.getAlias());
        if (ObjectUtils.isEmpty(variableContext.getVariables())) return;
        eventContext.addVariables(variableContext.getVariables());
    }
}