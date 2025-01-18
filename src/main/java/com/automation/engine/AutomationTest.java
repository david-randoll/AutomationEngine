package com.automation.engine;

import com.automation.engine.actions.IAction;
import com.automation.engine.actions.LoggerAction;
import com.automation.engine.conditions.ICondition;
import com.automation.engine.engine.Automation;
import com.automation.engine.engine.AutomationEngine;
import com.automation.engine.events.TimeEvent;
import com.automation.engine.triggers.ITrigger;
import com.automation.engine.triggers.TimeBasedTrigger;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutomationTest {
    private final AutomationEngine engine;

    @PostConstruct
    public void init() {
        List<ITrigger> triggers = List.of(new TimeBasedTrigger(LocalTime.of(14, 43), LocalTime.of(15, 59)));
        List<ICondition> conditions = List.of();
        List<IAction> actions = List.of(new LoggerAction("Hello, world!"));
        var automation = new Automation("Time based automation", triggers, conditions, actions);
        engine.addAutomation(automation);
    }

    @Scheduled(fixedRate = 1000)
    public void run() {
        engine.processEvent(new TimeEvent(LocalTime.now()));
    }
}