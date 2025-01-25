package com.automation.engine.engine.factory;

import com.automation.engine.engine.core.Automation;
import com.automation.engine.engine.factory.resolver.IAutomationResolver;
import com.automation.engine.engine.factory.resolver.ManualAutomationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class AutomationFactory {
    private final ManualAutomationResolver manualAutomationResolver;
    private final Map<String, IAutomationResolver<?>> resolverMap;

    public Automation createAutomation(CreateAutomation createAutomation) {
        return manualAutomationResolver.create(createAutomation);
    }

    public Automation createAutomation(String format, Object input) {
        IAutomationResolver<?> resolver = resolverMap.get(format + "AutomationResolver");
        if (resolver == null) {
            throw new RuntimeException("Resolver not found for format: " + format);
        }

        return ((IAutomationResolver<Object>) resolver).create(input);
    }
}