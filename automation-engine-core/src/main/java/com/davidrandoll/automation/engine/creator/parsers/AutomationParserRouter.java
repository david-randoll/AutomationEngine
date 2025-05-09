package com.davidrandoll.automation.engine.creator.parsers;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.exceptions.FormatParserNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AutomationParserRouter {
    private final String suffix;
    private final Map<String, IAutomationFormatParser<?>> formatParsers;

    public AutomationParserRouter(String suffix, Map<String, IAutomationFormatParser<?>> formatParsers) {
        this.formatParsers = formatParsers;
        this.suffix = suffix;
    }

    public AutomationParserRouter(Map<String, IAutomationFormatParser<?>> formatParsers) {
        this.formatParsers = formatParsers;
        this.suffix = "AutomationParser";
    }

    @SuppressWarnings("unchecked")
    private IAutomationFormatParser<Object> getFormatParser(String format) {
        IAutomationFormatParser<?> formatParser = formatParsers.get(format + suffix);
        if (formatParser == null) {
            throw new FormatParserNotFoundException(format);
        }
        return (IAutomationFormatParser<Object>) formatParser;
    }

    public Automation create(String format, Object input) {
        return getFormatParser(format).create(input);
    }
}