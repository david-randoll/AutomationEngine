package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserDefinedAction extends PluggableAction<UserDefinedActionContext> {
    private final IUserDefinedActionRegistry registry;

    @Override
    public ActionResult executeWithResult(EventContext ec, UserDefinedActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getName())) {
            log.warn("User-defined action name is not specified");
            return ActionResult.CONTINUE;
        }

        Optional<UserDefinedActionDefinition> actionDefinition = registry.findAction(ac.getName());
        if (actionDefinition.isEmpty()) {
            if (ac.isThrowErrorIfNotFound()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User-defined action '" + ac.getName() + "' not found in registry");
            }
            log.warn("User-defined action '{}' not found in registry", ac.getName());
            return ActionResult.CONTINUE;
        }

        UserDefinedActionDefinition definition = actionDefinition.get();
        log.debug("Executing user-defined action: {}", ac.getName());
        var eventContext = new EventContext(ec.getEvent());
        eventContext.addMetadata(new HashMap<>(ec.getMetadata()));

        // Merge default parameters with provided parameters
        // Default parameters from definition come first, then override with provided parameters
        if (!ObjectUtils.isEmpty(definition.getParameters())) {
            eventContext.addMetadata(definition.getParameters());
        }
        if (!ObjectUtils.isEmpty(ac.getParameters())) {
            eventContext.addMetadata(ac.getParameters());
        }

        // Resolve variables
        if (!ObjectUtils.isEmpty(definition.getVariables())) {
            processor.resolveVariables(eventContext, definition.getVariables());
        }

        // Check conditions
        if (!ObjectUtils.isEmpty(definition.getConditions())) {
            boolean conditionsSatisfied = processor.allConditionsSatisfied(eventContext, definition.getConditions());
            if (!conditionsSatisfied) {
                log.debug("Conditions not satisfied for user-defined action: {}", ac.getName());
                return ActionResult.CONTINUE;
            }
        }

        // Execute actions
        ActionResult actionResult = ActionResult.CONTINUE;
        if (!ObjectUtils.isEmpty(definition.getActions())) {
            actionResult = processor.executeActions(eventContext, definition.getActions());
        }

        if (!ObjectUtils.isEmpty(ac.getStoreToVariable())) {
            var result = processor.resolveResult(eventContext, definition.getResult());
            ec.addMetadata(ac.getStoreToVariable(), result);
        }

        log.debug("Completed user-defined action: {}", ac.getName());
        return actionResult;
    }

    @Override
    public void doExecute(EventContext ec, UserDefinedActionContext ac) {
        // No-op, using executeWithResult instead
    }
}