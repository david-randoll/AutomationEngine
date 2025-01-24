package com.automation.engine.engine.factory;

import com.automation.engine.engine.core.Automation;
import com.automation.engine.engine.factory.resolver.JsonAutomationResolver;
import com.automation.engine.engine.factory.resolver.ManualAutomationResolver;
import com.automation.engine.engine.factory.resolver.YamlAutomationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationFactory {
    private final ManualAutomationResolver manualAutomationResolver;
    private final YamlAutomationResolver yamlAutomationResolver;
    private final JsonAutomationResolver jsonAutomationResolver;

    public Automation createAutomation(CreateAutomation createAutomation) {
        return manualAutomationResolver.createAutomation(createAutomation);
    }

    public Automation createAutomationFromYaml(Object yaml) {
        return yamlAutomationResolver.createAutomation(yaml);
    }

    public Automation createAutomationFromJson(Object json) {
        return jsonAutomationResolver.createAutomation(json);
    }
}