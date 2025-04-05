package com.automation.engine.modules.conditions.time_based;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeCondition")
public class TimeBasedCondition extends AbstractCondition<TimeBasedConditionContext> {

    @Override
    public boolean isSatisfied(EventContext eventContext, TimeBasedConditionContext conditionContext) {
        if (!(eventContext.getEvent() instanceof TimeBasedEvent timeBasedEvent)) return false;

        LocalTime eventTime = timeBasedEvent.getTime();
        LocalTime beforeTime = conditionContext.getBefore();
        LocalTime afterTime = conditionContext.getAfter();

        if (beforeTime == null && afterTime == null) return false;
        boolean isBeforeConditionMet;
        boolean isAfterConditionMet;
        if (conditionContext.isInclusive()) {
            isBeforeConditionMet = (beforeTime == null || !eventTime.isAfter(beforeTime));
            isAfterConditionMet = (afterTime == null || !eventTime.isBefore(afterTime));
        } else {
            isBeforeConditionMet = (beforeTime == null || eventTime.isBefore(beforeTime));
            isAfterConditionMet = (afterTime == null || eventTime.isAfter(afterTime));
        }

        return isBeforeConditionMet && isAfterConditionMet;
    }
}