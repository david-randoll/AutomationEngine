package com.automation.engine.creator;

import com.automation.engine.core.Automation;
import com.automation.engine.creator.parsers.IAutomationFormatParser;
import com.automation.engine.creator.parsers.ManualAutomationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class AutomationCreator {
    private final ManualAutomationBuilder builder;
    private final Map<String, IAutomationFormatParser<?>> formatParsers;

    public Automation createAutomation(CreateAutomationRequest createRequest) {
        return builder.create(createRequest);
    }

    public Automation createAutomation(String format, Object input) {
        IAutomationFormatParser<?> resolver = formatParsers.get(format + "AutomationParser");
        if (resolver == null) {
            throw new RuntimeException("Resolver not found for format: " + format);
        }

        return ((IAutomationFormatParser<Object>) resolver).create(input);
    }
}