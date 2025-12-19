package com.davidrandoll.automation.engine.creator.parsers;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.AutomationProcessor;
import com.davidrandoll.automation.engine.creator.AutomationDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ManualAutomationBuilder implements IAutomationFormatParser<AutomationDefinition> {
    private final AutomationProcessor processor;

    @Override
    public Automation create(AutomationDefinition request) {
        log.info("Start creating automation: {}", request.getAlias());
        var automation = new Automation(
                request.getAlias(),
                request.getOptions(),
                processor.resolveVariables(request.getVariables()),
                processor.resolveTriggers(request.getTriggers()),
                processor.resolveConditions(request.getConditions()),
                processor.resolveActions(request.getActions()),
                processor.resolveResult(request.getResult()));
        log.info("Automation {} created successfully", request.getAlias());
        return automation;
    }
}