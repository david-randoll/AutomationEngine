package com.automation.engine.modules.time_based.condition;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.modules.time_based.event.TimeBasedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeBasedCondition")
public class TimeBasedCondition extends AbstractCondition<TimeBasedConditionContext> {

    @Override
    public boolean isSatisfied(Event event, TimeBasedConditionContext context) {
        if (!(event instanceof TimeBasedEvent timeBasedEvent)) return false;

        LocalTime eventTime = timeBasedEvent.getTime();
        LocalTime beforeTime = context.getBefore();
        LocalTime afterTime = context.getAfter();

        if (beforeTime == null && afterTime == null) return false;
        return (beforeTime == null || eventTime.isAfter(beforeTime)) && (afterTime == null || eventTime.isBefore(afterTime));
    }
}