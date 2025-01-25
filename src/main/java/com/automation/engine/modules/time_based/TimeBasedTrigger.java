package com.automation.engine.modules.time_based;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.AbstractTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeBasedTrigger")
public class TimeBasedTrigger extends AbstractTrigger<TimeBasedTriggerContext> {

    @Override
    public boolean isTriggered(Event event, TimeBasedTriggerContext triggerContext) {
        var result = false;
        if (event instanceof TimeBasedEvent timeBasedEvent) {
            LocalTime eventTime = timeBasedEvent.getTime();
            LocalTime beforeTime = triggerContext.getBeforeTime();
            LocalTime afterTime = triggerContext.getAfterTime();

            if (beforeTime == null && afterTime == null) return result;
            result = (beforeTime == null || eventTime.isAfter(beforeTime)) && (afterTime == null || eventTime.isBefore(afterTime));
        }
        return result;
    }
}