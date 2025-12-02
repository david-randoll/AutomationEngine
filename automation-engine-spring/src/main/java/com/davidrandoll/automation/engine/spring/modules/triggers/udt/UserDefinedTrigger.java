package com.davidrandoll.automation.engine.spring.modules.triggers.udt;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
public class UserDefinedTrigger extends PluggableTrigger<UserDefinedTriggerContext> {
    private final IUserDefinedTriggerRegistry registry;

    @Override
    public boolean isTriggered(EventContext ec, UserDefinedTriggerContext tc) {
        if (ObjectUtils.isEmpty(tc.getName())) {
            log.warn("User-defined trigger name is not specified");
            return false;
        }

        var triggerDefinition = registry.findTrigger(tc.getName());
        if (triggerDefinition.isEmpty()) {
            log.warn("User-defined trigger '{}' not found in registry", tc.getName());
            return false;
        }

        var definition = triggerDefinition.get();
        log.debug("Evaluating user-defined trigger: {}", tc.getName());

        // Add parameters to event context
        if (!ObjectUtils.isEmpty(tc.getParameters())) {
            ec.addMetadata(tc.getParameters());
        }

        // Resolve variables
        if (!ObjectUtils.isEmpty(definition.getVariables())) {
            processor.resolveVariables(ec, definition.getVariables());
        }

        // Check triggers
        if (ObjectUtils.isEmpty(definition.getTriggers())) {
            log.debug("No triggers defined for user-defined trigger: {}", tc.getName());
            return true;
        }

        return processor.anyTriggersTriggered(ec, definition.getTriggers());
    }
}

