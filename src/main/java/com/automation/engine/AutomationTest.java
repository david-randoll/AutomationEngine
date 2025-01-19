package com.automation.engine;

import com.automation.engine.engine.actions.ActionContext;
import com.automation.engine.engine.actions.IAction;
import com.automation.engine.engine.conditions.ICondition;
import com.automation.engine.engine.Automation;
import com.automation.engine.engine.AutomationEngine;
import com.automation.engine.engine.triggers.ITrigger;
import com.automation.engine.engine.triggers.TriggerContext;
import com.automation.engine.modules.time_based.TimeBasedEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutomationTest {
    private final AutomationEngine engine;
    private final Map<String, IAction> actions;
    private final Map<String, ICondition> conditions;
    private final Map<String, ITrigger> triggers;

    @PostConstruct
    public void init() {
        // Trigger
        ITrigger timeBasedTrigger = triggers.get("timeBasedTrigger");
        ITrigger timeBasedTriggerWithContext = event -> timeBasedTrigger.isTriggered(event, new TriggerContext(Map.of(
                "beforeTime", LocalTime.of(0, 0),
                "afterTime", LocalTime.of(23, 59)
        )));

        // Action
        IAction loggerAction = actions.get("loggerAction");
        IAction loggerActionWithContext = context -> loggerAction.execute(context, new ActionContext(Map.of(
                "message", "Time based automation triggered"
        )));

        List<ITrigger> triggers = List.of(timeBasedTriggerWithContext);
        List<ICondition> conditions = List.of();
        List<IAction> actions = List.of(loggerActionWithContext);
        var automation = new Automation("Time based automation", triggers, conditions, actions);
        engine.addAutomation(automation);
    }

    @Scheduled(fixedRate = 1000)
    public void run() {
        engine.processEvent(new TimeBasedEvent(LocalTime.now()));
    }
}