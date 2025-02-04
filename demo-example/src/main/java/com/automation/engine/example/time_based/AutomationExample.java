package com.automation.engine.example.time_based;

import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.BaseActionList;
import com.automation.engine.core.actions.IBaseAction;
import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.actions.interceptors.InterceptingAction;
import com.automation.engine.core.conditions.BaseConditionList;
import com.automation.engine.core.conditions.IBaseCondition;
import com.automation.engine.core.triggers.BaseTriggerList;
import com.automation.engine.core.triggers.IBaseTrigger;
import com.automation.engine.core.triggers.TriggerContext;
import com.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.core.triggers.interceptors.InterceptingTrigger;
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
public class AutomationExample {
    private final AutomationEngine engine;
    private final Map<String, IBaseAction> actions;
    private final Map<String, IBaseCondition> conditions;
    private final Map<String, IBaseTrigger> triggers;

    private final List<IActionInterceptor> actionInterceptors;
    private final List<ITriggerInterceptor> triggerInterceptors;

    @PostConstruct
    public void init() {
        BaseTriggerList triggers = BaseTriggerList.of(getTrigger());
        BaseConditionList conditions = BaseConditionList.of();
        BaseActionList actions = BaseActionList.of(getAction());
        var automation = new Automation("Time based automation", triggers, conditions, actions);
        engine.addAutomation(automation);
    }

    private IBaseTrigger getTrigger() {
        IBaseTrigger timeBasedTrigger = triggers.get("timeTrigger");
        var interceptingTrigger = new InterceptingTrigger(timeBasedTrigger, triggerInterceptors);
        return event -> interceptingTrigger.isTriggered(event, new TriggerContext(Map.of(
                "at", LocalTime.of(23, 50)
        )));
    }

    private IBaseAction getAction() {
        IBaseAction loggerAction = actions.get("loggerAction");
        var interceptingAction = new InterceptingAction(loggerAction, actionInterceptors);

        return context -> interceptingAction.execute(context, new ActionContext(Map.of(
                "message", "Time based automation triggered at {{ time | time_format(pattern='hh:mm a') }}"
        )));
    }
}