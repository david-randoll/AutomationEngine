package com.davidrandoll.automation.engine.creator.parsers.json;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.CreateAutomationRequest;
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
        CreateAutomationRequest createRequest = converter.convert(json, CreateAutomationRequest.class);
        return builder.create(createRequest);
    }
}