package com.davidrandoll.automation.engine.spring.modules.variables.udv;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
public class UserDefinedVariable extends PluggableVariable<UserDefinedVariableContext> {
    private final IUserDefinedVariableRegistry registry;

    @Override
    public void resolve(EventContext ec, UserDefinedVariableContext vc) {
        if (ObjectUtils.isEmpty(vc.getName())) {
            log.warn("User-defined variable name is not specified");
            return;
        }

        var variableDefinition = registry.findVariable(vc.getName());
        if (variableDefinition.isEmpty()) {
            if (vc.isThrowErrorIfNotFound())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User-defined variable '" + vc.getName() + "' not found in registry");
            log.warn("User-defined variable '{}' not found in registry", vc.getName());
            return;
        }

        var definition = variableDefinition.get();
        log.debug("Resolving user-defined variable: {}", vc.getName());

        // Merge default parameters with provided parameters
        // Default parameters from definition come first, then override with provided parameters
        if (!ObjectUtils.isEmpty(definition.getParameters())) {
            ec.addMetadata(definition.getParameters());
        }
        if (!ObjectUtils.isEmpty(vc.getParameters())) {
            ec.addMetadata(vc.getParameters());
        }

        // Resolve variables
        if (!ObjectUtils.isEmpty(definition.getVariables())) {
            processor.resolveVariables(ec, definition.getVariables());
        }
    }
}

