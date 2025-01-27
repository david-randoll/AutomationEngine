package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;
import org.springframework.lang.NonNull;

public interface IAutomationResolver<T> {
    @NonNull
    Automation create(T input);
}