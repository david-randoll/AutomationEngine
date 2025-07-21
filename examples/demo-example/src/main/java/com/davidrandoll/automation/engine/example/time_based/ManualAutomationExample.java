package com.davidrandoll.automation.engine.example.time_based;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.creator.AutomationDefinition;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
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
        var trigger = TriggerDefinition.builder()
                .trigger("time")
                .params(Map.of(
                        "at", LocalTime.of(23, 50)
                ))
                .build();
        var action = ActionDefinition.builder()
                .action("logger")
                .params(Map.of(
                        "message", "Manual automation triggered at {{ time | time_format(pattern='hh:mm a') }}"
                ))
                .build();
        var request = AutomationDefinition.builder()
                .alias("Manual Automation Test")
                .triggers(List.of(trigger))
                .actions(List.of(action))
                .build();
        var automation = factory.createAutomation(request);
        engine.register(automation);
    }
}