package com.automation.engine.modules.triggers.time_based;

import com.automation.engine.conditional.AEConditionalOnMissingBeanName;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.automation.engine.spi.PluggableTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component("timeTrigger")
@AEConditionalOnMissingBeanName
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