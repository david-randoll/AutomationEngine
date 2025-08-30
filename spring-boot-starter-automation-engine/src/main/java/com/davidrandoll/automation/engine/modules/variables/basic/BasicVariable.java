package com.davidrandoll.automation.engine.modules.variables.basic;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableVariable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Slf4j
public class BasicVariable extends PluggableVariable<BasicVariableContext> {

    @Override
    public void resolve(EventContext eventContext, BasicVariableContext variableContext) {
        log.info("Resolving basic variable: {}", variableContext.getAlias());
        if (ObjectUtils.isEmpty(variableContext.getVariables())) return;
        eventContext.addMetadata(variableContext.getVariables());
    }
}