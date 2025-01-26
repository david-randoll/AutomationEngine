package com.automation.engine.example.time_based;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
import com.automation.engine.factory.CreateRequest;
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
public class ManualAutomationExample {
    private final AutomationEngine engine;
    private final AutomationFactory factory;

    @PostConstruct
    public void init() {
        var trigger = CreateRequest.Trigger.builder()
                .trigger("timeBasedTrigger")
                .params(Map.of(
                        "at", LocalTime.of(0, 0)
                ))
                .build();
        var action = CreateRequest.Action.builder()
                .action("loggerAction")
                .params(Map.of(
                        "message", "Manual automation triggered at {{ time | time_format(pattern='hh:mm a') }}"
                ))
                .build();
        var request = CreateRequest.builder()
                .alias("Manual Automation Test")
                .triggers(List.of(trigger))
                .actions(List.of(action))
                .build();
        var automation = factory.createAutomation(request);
        engine.addAutomation(automation);
    }
}