package com.davidrandoll.automation.engine.example.time_based;

import com.davidrandoll.automation.engine.core.AutomationEngine;
import com.davidrandoll.automation.engine.creator.AutomationCreator;
import com.davidrandoll.automation.engine.creator.CreateAutomationRequest;
import com.davidrandoll.automation.engine.creator.actions.Action;
import com.davidrandoll.automation.engine.creator.triggers.Trigger;
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
        var request = CreateAutomationRequest.builder()
                .alias("Manual Automation Test")
                .triggers(List.of(trigger))
                .actions(List.of(action))
                .build();
        var automation = factory.createAutomation(request);
        engine.register(automation);
    }
}