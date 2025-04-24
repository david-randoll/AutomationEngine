package com.automation.engine.modules.conditions.time_based;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.automation.engine.spi.PluggableCondition;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeCondition")
public class TimeBasedCondition extends PluggableCondition<TimeBasedConditionContext> {

    @Override
    public boolean isSatisfied(EventContext ec, TimeBasedConditionContext cc) {
        if (!(ec.getEvent() instanceof TimeBasedEvent timeBasedEvent)) return false;

        LocalTime eventTime = timeBasedEvent.getTime();
        LocalTime beforeTime = cc.getBefore();
        LocalTime afterTime = cc.getAfter();

        if (beforeTime == null && afterTime == null) return false;
        boolean isBeforeConditionMet;
        boolean isAfterConditionMet;
        if (cc.isInclusive()) {
            isBeforeConditionMet = (beforeTime == null || !eventTime.isAfter(beforeTime));
            isAfterConditionMet = (afterTime == null || !eventTime.isBefore(afterTime));
        } else {
            isBeforeConditionMet = (beforeTime == null || eventTime.isBefore(beforeTime));
            isAfterConditionMet = (afterTime == null || eventTime.isAfter(afterTime));
        }

        return isBeforeConditionMet && isAfterConditionMet;
    }
}