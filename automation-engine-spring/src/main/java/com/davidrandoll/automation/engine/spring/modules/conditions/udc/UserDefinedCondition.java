package com.davidrandoll.automation.engine.spring.modules.conditions.udc;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
public class UserDefinedCondition extends PluggableCondition<UserDefinedConditionContext> {
    private final IUserDefinedConditionRegistry registry;

    @Override
    public boolean isSatisfied(EventContext ec, UserDefinedConditionContext cc) {
        if (ObjectUtils.isEmpty(cc.getName())) {
            log.warn("User-defined condition name is not specified");
            return false;
        }

        var conditionDefinition = registry.findCondition(cc.getName());
        if (conditionDefinition.isEmpty()) {
            if (cc.isThrowErrorIfNotFound())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User-defined condition '" + cc.getName() + "' not found in registry");
            log.warn("User-defined condition '{}' not found in registry", cc.getName());
            return false;
        }

        var definition = conditionDefinition.get();
        log.debug("Evaluating user-defined condition: {}", cc.getName());

        // Merge default parameters with provided parameters
        // Default parameters from definition come first, then override with provided parameters
        if (!ObjectUtils.isEmpty(definition.getParameters())) {
            ec.addMetadata(definition.getParameters());
        }
        if (!ObjectUtils.isEmpty(cc.getParameters())) {
            ec.addMetadata(cc.getParameters());
        }

        // Resolve variables
        if (!ObjectUtils.isEmpty(definition.getVariables())) {
            processor.resolveVariables(ec, definition.getVariables());
        }

        // Check conditions
        if (ObjectUtils.isEmpty(definition.getConditions())) {
            log.debug("No conditions defined for user-defined condition: {}", cc.getName());
            return true;
        }

        return processor.allConditionsSatisfied(ec, definition.getConditions());
    }
}

