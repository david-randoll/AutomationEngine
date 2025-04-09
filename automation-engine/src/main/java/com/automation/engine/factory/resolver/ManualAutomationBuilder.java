package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;
import com.automation.engine.factory.AutomationProcessor;
import com.automation.engine.factory.CreateAutomationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service("manualAutomationParser")
@RequiredArgsConstructor
public class ManualAutomationBuilder implements IAutomationFormatParser<CreateAutomationRequest> {
    private final AutomationProcessor processor;

    @Override
    @NonNull
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