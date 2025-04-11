package com.automation.engine.creator.parsers.yaml;

import com.automation.engine.core.Automation;
import com.automation.engine.creator.CreateAutomationRequest;
import com.automation.engine.creator.parsers.IAutomationFormatParser;
import com.automation.engine.creator.parsers.ManualAutomationBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YamlAutomationParser implements IAutomationFormatParser<String> {
    private final ManualAutomationBuilder builder;
    private final IYamlConverter converter;

    @Override
    public Automation create(String yaml) {
        CreateAutomationRequest createRequest = converter.convert(yaml, CreateAutomationRequest.class);
        return builder.create(createRequest);
    }
}