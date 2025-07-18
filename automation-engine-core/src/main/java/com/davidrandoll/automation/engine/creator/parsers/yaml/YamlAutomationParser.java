package com.davidrandoll.automation.engine.creator.parsers.yaml;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.AutomationDefinition;
import com.davidrandoll.automation.engine.creator.parsers.IAutomationFormatParser;
import com.davidrandoll.automation.engine.creator.parsers.ManualAutomationBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YamlAutomationParser implements IAutomationFormatParser<String> {
    private final ManualAutomationBuilder builder;
    private final IYamlConverter converter;

    @Override
    public Automation create(String yaml) {
        AutomationDefinition createRequest = converter.convert(yaml, AutomationDefinition.class);
        return builder.create(createRequest);
    }
}