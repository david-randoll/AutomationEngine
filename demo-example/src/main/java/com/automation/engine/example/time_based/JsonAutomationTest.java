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
public class JsonAutomationTest {
    private final AutomationEngine engine;
    private final AutomationFactory factory;

    @PostConstruct
    public void init() {
        var json = """
                {
                  "alias": "Json Automation Test",
                  "triggers": [
                    {
                      "trigger": "timeBasedTrigger",
                      "at": "00:00"
                    }
                  ],
                  "actions": [
                    {
                      "action": "loggerAction",
                      "message": "Json automation triggered at {{ time | time_format(pattern='hh:mm a') }}"
                    }
                  ]
                }
                """;

        var automation = factory.createAutomation("json", json);
        engine.addAutomation(automation);
    }
}