package com.davidrandoll.automation.engine.creator.parsers.json;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.AutomationDefinition;
import com.davidrandoll.automation.engine.creator.parsers.IAutomationFormatParser;
import com.davidrandoll.automation.engine.creator.parsers.ManualAutomationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JsonAutomationParser implements IAutomationFormatParser<String> {
    private final ManualAutomationBuilder builder;
    private final IJsonConverter converter;

    @Override
    public Automation create(String json) {
        AutomationDefinition createRequest = converter.convert(json, AutomationDefinition.class);
        return builder.create(createRequest);
    }
}