package com.davidrandoll.automation.engine.creator.parsers;

import com.davidrandoll.automation.engine.core.Automation;

public interface IAutomationFormatParser<T> {
    Automation create(T input);
}