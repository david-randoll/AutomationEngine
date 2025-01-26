package com.automation.engine.modules.time_based.trigger;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.AbstractTrigger;
import com.automation.engine.modules.time_based.event.TimeBasedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeBasedTrigger")
public class TimeBasedTrigger extends AbstractTrigger<TimeBasedTriggerContext> {

    @Override
    public boolean isTriggered(Event event, TimeBasedTriggerContext context) {
        if (!(event instanceof TimeBasedEvent timeBasedEvent)) return false;

        LocalTime eventTime = timeBasedEvent.getTime();
        LocalTime beforeTime = context.getBeforeTime();
        LocalTime afterTime = context.getAfterTime();

        if (beforeTime == null && afterTime == null) return false;
        return (beforeTime == null || eventTime.isAfter(beforeTime)) && (afterTime == null || eventTime.isBefore(afterTime));
    }
}