package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;

public interface IAutomationResolver<T> {
    Automation create(T input);
}