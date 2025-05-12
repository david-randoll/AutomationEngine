package com.davidrandoll.automation.engine.example.time_based;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.AutomationEngine;
import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.BaseActionList;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.actions.IBaseAction;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.actions.interceptors.InterceptingAction;
import com.davidrandoll.automation.engine.core.conditions.BaseConditionList;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.result.IBaseResult;
import com.davidrandoll.automation.engine.core.triggers.BaseTriggerList;
import com.davidrandoll.automation.engine.core.triggers.IBaseTrigger;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.core.triggers.interceptors.InterceptingTrigger;
import com.davidrandoll.automation.engine.core.variables.BaseVariableList;
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
    private final Map<String, IAction> actions;
    private final Map<String, ICondition> conditions;
    private final Map<String, ITrigger> triggers;

    private final List<IActionInterceptor> actionInterceptors;
    private final List<ITriggerInterceptor> triggerInterceptors;

    @PostConstruct
    public void init() {
        BaseTriggerList triggersList = BaseTriggerList.of(getTrigger());
        BaseConditionList conditionsList = BaseConditionList.of();
        BaseActionList actionsList = BaseActionList.of(getAction());
        BaseVariableList variablesList = BaseVariableList.of();
        IBaseResult result = e -> null;
        var automation = new Automation("Time based automation", variablesList, triggersList, conditionsList, actionsList, result);
        engine.register(automation);
    }

    private IBaseTrigger getTrigger() {
        var timeBasedTrigger = triggers.get("timeTrigger");
        var interceptingTrigger = new InterceptingTrigger(timeBasedTrigger, triggerInterceptors);
        return event -> interceptingTrigger.isTriggered(event, new TriggerContext(Map.of(
                "at", LocalTime.of(23, 50)
        )));
    }

    private IBaseAction getAction() {
        IAction loggerAction = actions.get("loggerAction");
        var interceptingAction = new InterceptingAction(loggerAction, actionInterceptors);

        return context -> interceptingAction.execute(context, new ActionContext(Map.of(
                "message", "Time based automation triggered at {{ time | time_format(pattern='hh:mm a') }}"
        )));
    }
}