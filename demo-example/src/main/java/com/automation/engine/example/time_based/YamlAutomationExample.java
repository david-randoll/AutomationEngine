package com.automation.engine.example.time_based;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YamlAutomationExample {
    private final AutomationEngine engine;
    private final AutomationFactory factory;

    @PostConstruct
    public void init() {
        var yaml = """
                alias: Yaml Automation Test
                triggers:
                  - trigger: timeBasedTrigger
                    at: 00:03
                actions:
                  - action: loggerAction
                    message: Yaml automation triggered at {{ time | time_format(pattern='hh:mm a') }}
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);
    }
}