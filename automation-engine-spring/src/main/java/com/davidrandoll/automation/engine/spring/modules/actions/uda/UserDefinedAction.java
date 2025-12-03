package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
public class UserDefinedAction extends PluggableAction<UserDefinedActionContext> {
    private final IUserDefinedActionRegistry registry;

    @Override
    public void doExecute(EventContext ec, UserDefinedActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getName())) {
            log.warn("User-defined action name is not specified");
            return;
        }

        var actionDefinition = registry.findAction(ac.getName());
        if (actionDefinition.isEmpty()) {
            log.warn("User-defined action '{}' not found in registry", ac.getName());
            return;
        }

        UserDefinedActionDefinition definition = actionDefinition.get();
        log.debug("Executing user-defined action: {}", ac.getName());

        // Merge default parameters with provided parameters
        // Default parameters from definition come first, then override with provided parameters
        if (!ObjectUtils.isEmpty(definition.getParameters())) {
            ec.addMetadata(definition.getParameters());
        }
        if (!ObjectUtils.isEmpty(ac.getParameters())) {
            ec.addMetadata(ac.getParameters());
        }

        // Resolve variables
        if (!ObjectUtils.isEmpty(definition.getVariables())) {
            processor.resolveVariables(ec, definition.getVariables());
        }

        // Check conditions
        if (!ObjectUtils.isEmpty(definition.getConditions())) {
            boolean conditionsSatisfied = processor.allConditionsSatisfied(ec, definition.getConditions());
            if (!conditionsSatisfied) {
                log.debug("Conditions not satisfied for user-defined action: {}", ac.getName());
                return;
            }
        }

        // Execute actions
        if (!ObjectUtils.isEmpty(definition.getActions())) {
            processor.executeActions(ec, definition.getActions());
        }
    }
}