package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;
import com.automation.engine.core.actions.BaseActionList;
import com.automation.engine.core.conditions.BaseConditionList;
import com.automation.engine.core.triggers.BaseTriggerList;
import com.automation.engine.core.variables.BaseVariableList;
import com.automation.engine.factory.AutomationProcessor;
import com.automation.engine.factory.CreateAutomationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service("manualAutomationResolver")
@RequiredArgsConstructor
public class DefaultAutomationResolver implements IAutomationResolver<CreateAutomationRequest> {
    private final AutomationProcessor resolver;

    @Override
    @NonNull
    public Automation create(CreateAutomationRequest createRequest) {
        log.info("Start creating automation: {}", createRequest.getAlias());
        var alias = createRequest.getAlias();
        BaseVariableList variables = resolver.resolveVariables(createRequest.getVariables());
        BaseTriggerList triggers = resolver.resolveTriggers(createRequest.getTriggers());
        BaseConditionList conditions = resolver.resolveConditions(createRequest.getConditions());
        BaseActionList actions = resolver.resolveActions(createRequest.getActions());
        var automation = new Automation(alias, variables, triggers, conditions, actions);
        log.info("Automation {} created successfully", alias);
        return automation;
    }
}