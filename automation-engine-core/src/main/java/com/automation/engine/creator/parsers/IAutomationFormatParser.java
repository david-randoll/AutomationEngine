package com.automation.engine.creator.parsers;

import com.automation.engine.core.Automation;

public interface IAutomationFormatParser<T> {
    Automation create(T input);
}