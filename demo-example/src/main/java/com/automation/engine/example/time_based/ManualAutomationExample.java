package com.automation.engine.example.time_based;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationCreator;
import com.automation.engine.core.factory.model.Action;
import com.automation.engine.core.factory.model.CreateRequest;
import com.automation.engine.core.factory.model.Trigger;
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
    private final AutomationCreator factory;

    @PostConstruct
    public void init() {
        var trigger = Trigger.builder()
                .trigger("time")
                .params(Map.of(
                        "at", LocalTime.of(23, 50)
                ))
                .build();
        var action = Action.builder()
                .action("logger")
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
        engine.register(automation);
    }
}