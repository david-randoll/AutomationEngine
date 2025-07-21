package com.davidrandoll.automation.engine.example.time_based;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
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