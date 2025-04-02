package com.automation.engine.modules.triggers.time_based;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.triggers.AbstractTrigger;
import com.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeTrigger")
public class TimeBasedTrigger extends AbstractTrigger<TimeBasedTriggerContext> {

    @Override
    public boolean isTriggered(EventContext eventContext, TimeBasedTriggerContext triggerContext) {
        if (!(eventContext.getEvent() instanceof TimeBasedEvent timeBasedEvent)) return false;

        LocalTime eventTime = timeBasedEvent.getTime();
        LocalTime atTime = triggerContext.getAt();

        if (atTime == null) return false;

        return getNormalizedTime(eventTime).equals(getNormalizedTime(atTime));
    }

    private LocalTime getNormalizedTime(LocalTime time) {
        return time.withNano(0).withSecond(0);
    }
}