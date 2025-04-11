package com.automation.engine.example.time_based;

import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.creator.AutomationCreator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YamlAutomationExample {
    private final AutomationEngine engine;
    private final AutomationCreator factory;

    @PostConstruct
    public void init() {
        var yaml = """
                alias: Yaml Automation Test
                triggers:
                  - trigger: time
                    at: 23:50
                actions:
                  - action: logger
                    message: Yaml automation triggered at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
    }
}