package com.davidrandoll.automation.engine.creator;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.exceptions.FormatParserNotFoundException;
import com.davidrandoll.automation.engine.creator.parsers.IAutomationFormatParser;
import com.davidrandoll.automation.engine.creator.parsers.ManualAutomationBuilder;
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
        IAutomationFormatParser<?> formatParser = formatParsers.get(format + "AutomationParser");
        if (formatParser == null) {
            throw new FormatParserNotFoundException(format);
        }

        return ((IAutomationFormatParser<Object>) formatParser).create(input);
    }
}