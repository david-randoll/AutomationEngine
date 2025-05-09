package com.davidrandoll.automation.engine.creator;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.parsers.AutomationParserRouter;
import com.davidrandoll.automation.engine.creator.parsers.ManualAutomationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AutomationCreator {
    private final ManualAutomationBuilder builder;
    private final AutomationParserRouter router;

    public Automation createAutomation(CreateAutomationRequest createRequest) {
        return builder.create(createRequest);
    }

    public Automation createAutomation(String format, Object input) {
        return router.create(format, input);
    }
}