package com.automation.engine.engine.factory.resolver;

import com.automation.engine.engine.core.Automation;

public interface IAutomationResolver<T> {
    Automation create(T input);
}