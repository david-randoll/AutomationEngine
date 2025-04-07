package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;
import com.automation.engine.core.actions.BaseActionList;
import com.automation.engine.core.conditions.BaseConditionList;
import com.automation.engine.core.triggers.BaseTriggerList;
import com.automation.engine.core.variables.BaseVariableList;
import com.automation.engine.factory.CreateAutomationRequest;
import com.automation.engine.factory.actions.ActionResolver;
import com.automation.engine.factory.conditions.ConditionResolver;
import com.automation.engine.factory.triggers.TriggerResolver;
import com.automation.engine.factory.variables.VariableResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service("manualAutomationResolver")
@RequiredArgsConstructor
public class DefaultAutomationResolver implements IAutomationResolver<CreateAutomationRequest> {
    private final ActionResolver actionResolver;
    private final ConditionResolver conditionResolver;
    private final VariableResolver variableResolver;
    private final TriggerResolver triggerResolver;

    @Override
    @NonNull
    public Automation create(CreateAutomationRequest createRequest) {
        log.info("Start creating automation: {}", createRequest.getAlias());
        var alias = createRequest.getAlias();
        BaseVariableList variables = variableResolver.resolve(createRequest.getVariables());
        BaseTriggerList triggers = triggerResolver.resolve(createRequest.getTriggers());
        BaseConditionList conditions = conditionResolver.resolve(createRequest.getConditions());
        BaseActionList actions = actionResolver.resolve(createRequest.getActions());
        var automation = new Automation(alias, variables, triggers, conditions, actions);
        log.info("Automation {} created successfully", alias);
        return automation;
    }
}