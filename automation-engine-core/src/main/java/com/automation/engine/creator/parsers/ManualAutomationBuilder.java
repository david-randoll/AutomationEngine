package com.automation.engine.creator.parsers;

import com.automation.engine.core.Automation;
import com.automation.engine.creator.AutomationProcessor;
import com.automation.engine.creator.CreateAutomationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ManualAutomationBuilder implements IAutomationFormatParser<CreateAutomationRequest> {
    private final AutomationProcessor processor;

    @Override
    public Automation create(CreateAutomationRequest request) {
        log.info("Start creating automation: {}", request.getAlias());
        var automation = new Automation(
                request.getAlias(),
                processor.resolveVariables(request.getVariables()),
                processor.resolveTriggers(request.getTriggers()),
                processor.resolveConditions(request.getConditions()),
                processor.resolveActions(request.getActions())
        );
        log.info("Automation {} created successfully", request.getAlias());
        return automation;
    }
}