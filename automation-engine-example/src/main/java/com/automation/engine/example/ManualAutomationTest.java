package com.automation.engine.example;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
import com.automation.engine.factory.CreateAutomation;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManualAutomationTest {
    private final AutomationEngine engine;
    private final AutomationFactory factory;

    @PostConstruct
    public void init() {
        var trigger = CreateAutomation.Trigger.builder()
                .trigger("timeBasedTrigger")
                .params(Map.of(
                        "beforeTime", LocalTime.of(0, 0),
                        "afterTime", LocalTime.of(23, 59)
                ))
                .build();
        var action = CreateAutomation.Action.builder()
                .action("loggerAction")
                .params(Map.of(
                        "message", "Manual automation triggered at {{ time | time_format(pattern='hh:mm a') }}"
                ))
                .build();
        var request = CreateAutomation.builder()
                .alias("Manual Automation Test")
                .triggers(List.of(trigger))
                .actions(List.of(action))
                .build();
        var automation = factory.createAutomation(request);
        engine.addAutomation(automation);
    }
}