package com.davidrandoll.automation.engine.modules.triggers.time_based;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeTrigger")
@ConditionalOnMissingBean(name = "timeTrigger", ignored = TimeBasedTrigger.class)
public class TimeBasedTrigger extends PluggableTrigger<TimeBasedTriggerContext> {

    @Override
    public boolean isTriggered(EventContext ec, TimeBasedTriggerContext tc) {
        if (!(ec.getEvent() instanceof TimeBasedEvent timeBasedEvent)) return false;

        LocalTime eventTime = timeBasedEvent.getTime();
        LocalTime atTime = tc.getAt();

        if (atTime == null) return false;

        return getNormalizedTime(eventTime).equals(getNormalizedTime(atTime));
    }

    private LocalTime getNormalizedTime(LocalTime time) {
        return time.withNano(0).withSecond(0);
    }
}