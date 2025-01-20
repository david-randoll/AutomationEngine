package com.automation.engine;

import com.automation.engine.engine.Automation;
import com.automation.engine.engine.AutomationEngine;
import com.automation.engine.engine.actions.ActionContext;
import com.automation.engine.engine.actions.IBaseAction;
import com.automation.engine.engine.actions.interceptors.IActionInterceptor;
import com.automation.engine.engine.actions.interceptors.InterceptingAction;
import com.automation.engine.engine.conditions.IBaseCondition;
import com.automation.engine.engine.triggers.IBaseTrigger;
import com.automation.engine.engine.triggers.TriggerContext;
import com.automation.engine.engine.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.engine.triggers.interceptors.InterceptingTrigger;
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
    private final Map<String, IBaseAction> actions;
    private final Map<String, IBaseCondition> conditions;
    private final Map<String, IBaseTrigger> triggers;

    private final List<IActionInterceptor> actionInterceptors;
    private final List<ITriggerInterceptor> triggerInterceptors;

    @PostConstruct
    public void init() {
        List<IBaseTrigger> triggers = List.of(getTrigger());
        List<IBaseCondition> conditions = List.of();
        List<IBaseAction> actions = List.of(getAction());
        var automation = new Automation("Time based automation", triggers, conditions, actions);
        engine.addAutomation(automation);
    }

    private IBaseTrigger getTrigger() {
        IBaseTrigger timeBasedTrigger = triggers.get("timeBasedTrigger");
        var interceptingTrigger = new InterceptingTrigger(timeBasedTrigger, triggerInterceptors);
        return event -> interceptingTrigger.isTriggered(event, new TriggerContext(Map.of(
                "beforeTime", LocalTime.of(0, 0),
                "afterTime", LocalTime.of(23, 59)
        )));
    }

    private IBaseAction getAction() {
        IBaseAction loggerAction = actions.get("loggerAction");
        var interceptingAction = new InterceptingAction(loggerAction, actionInterceptors);

        return context -> interceptingAction.execute(context, new ActionContext(Map.of(
                "message", "Time based automation triggered"
        )));
    }

    @Scheduled(fixedRate = 1000)
    public void run() {
        engine.processEvent(new TimeBasedEvent(LocalTime.now()));
    }
}