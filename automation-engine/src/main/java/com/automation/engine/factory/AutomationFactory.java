package com.automation.engine.factory;

import com.automation.engine.core.Automation;
import com.automation.engine.factory.model.CreateRequest;
import com.automation.engine.factory.resolver.IAutomationResolver;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class AutomationFactory {
    private final DefaultAutomationResolver defaultAutomationResolver;
    private final Map<String, IAutomationResolver<?>> resolverMap;

    public Automation createAutomation(CreateRequest createRequest) {
        return defaultAutomationResolver.create(createRequest);
    }

    public Automation createAutomation(String format, Object input) {
        IAutomationResolver<?> resolver = resolverMap.get(format + "AutomationResolver");
        if (resolver == null) {
            throw new RuntimeException("Resolver not found for format: " + format);
        }

        return ((IAutomationResolver<Object>) resolver).create(input);
    }
}