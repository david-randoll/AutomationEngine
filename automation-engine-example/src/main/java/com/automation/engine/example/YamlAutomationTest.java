package com.automation.engine.example;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YamlAutomationTest {
    private final AutomationEngine engine;
    private final AutomationFactory factory;

    @PostConstruct
    public void init() {
        var yaml = """
                alias: Yaml Automation Test
                triggers:
                  - trigger: timeBasedTrigger
                    beforeTime: 00:00
                    afterTime: 23:59
                actions:
                  - action: loggerAction
                    message: Yaml automation triggered at {{ time | time_format(pattern='hh:mm a') }}
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);
    }
}