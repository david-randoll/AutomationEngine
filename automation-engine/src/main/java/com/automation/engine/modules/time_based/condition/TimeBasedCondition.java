package com.automation.engine.modules.time_based.condition;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.modules.time_based.event.TimeBasedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeCondition")
public class TimeBasedCondition extends AbstractCondition<TimeBasedConditionContext> {

    @Override
    public boolean isSatisfied(Event event, TimeBasedConditionContext context) {
        if (!(event instanceof TimeBasedEvent timeBasedEvent)) return false;

        LocalTime eventTime = timeBasedEvent.getTime();
        LocalTime beforeTime = context.getBefore();
        LocalTime afterTime = context.getAfter();

        if (beforeTime == null && afterTime == null) return false;
        boolean isBeforeConditionMet;
        boolean isAfterConditionMet;
        if (context.isInclusive()) {
            isBeforeConditionMet = (beforeTime == null || !eventTime.isAfter(beforeTime));
            isAfterConditionMet = (afterTime == null || !eventTime.isBefore(afterTime));
        } else {
            isBeforeConditionMet = (beforeTime == null || eventTime.isBefore(beforeTime));
            isAfterConditionMet = (afterTime == null || eventTime.isAfter(afterTime));
        }

        return isBeforeConditionMet && isAfterConditionMet;
    }
}