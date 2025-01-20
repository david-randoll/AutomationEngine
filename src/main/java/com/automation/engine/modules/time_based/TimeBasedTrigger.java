package com.automation.engine.modules.time_based;

import com.automation.engine.engine.events.Event;
import com.automation.engine.engine.triggers.AbstractTrigger;
import com.automation.engine.engine.triggers.TriggerContext;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeBasedTrigger")
public class TimeBasedTrigger extends AbstractTrigger {

    @Override
    public boolean isTriggered(Event event, TriggerContext triggerContext) {
        var result = false;
        if (event instanceof TimeBasedEvent timeBasedEvent) {
            LocalTime eventTime = timeBasedEvent.getTime();
            var newTriggerContext = new TimeBasedTriggerContext(triggerContext);
            LocalTime beforeTime = newTriggerContext.getBeforeTime();
            LocalTime afterTime = newTriggerContext.getAfterTime();

            if (beforeTime == null && afterTime == null) return result;
            result = (beforeTime == null || eventTime.isAfter(beforeTime)) && (afterTime == null || eventTime.isBefore(afterTime));
        }
        return result;
    }
}