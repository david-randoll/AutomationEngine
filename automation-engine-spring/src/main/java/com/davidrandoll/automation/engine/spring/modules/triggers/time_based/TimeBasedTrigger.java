package com.davidrandoll.automation.engine.spring.modules.triggers.time_based;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEventPublisher;
import com.davidrandoll.automation.engine.spring.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;

/**
 * Trigger that activates at a specific time of day.
 *
 * <p>When this trigger is evaluated with a matching time, it automatically
 * schedules the next occurrence with the TimeBasedEventPublisher.
 */
@Slf4j
@RequiredArgsConstructor
public class TimeBasedTrigger extends PluggableTrigger<TimeBasedTriggerContext> {
    private final TimeBasedEventPublisher publisher;

    @Override
    public boolean isTriggered(EventContext ec, TimeBasedTriggerContext tc) {
        if (!(ec.getEvent() instanceof TimeBasedEvent timeBasedEvent)) return false;

        LocalTime eventTime = timeBasedEvent.getTime();
        LocalTime atTime = tc.getAt();

        if (atTime == null) return false;

        boolean matches = getNormalizedTime(eventTime).equals(getNormalizedTime(atTime));

        // If this trigger matches, schedule the next occurrence
        if (publisher != null) {
            String scheduleKey = getScheduleKey(tc);
            log.debug("Time-based trigger matched at {}. Scheduling next occurrence for '{}'.",
                    atTime, scheduleKey);
            publisher.scheduleAt(scheduleKey, atTime);
        }

        return matches;
    }

    /**
     * Creates a unique schedule key from the trigger context.
     * Uses the trigger alias if available, otherwise generates from time.
     */
    private String getScheduleKey(TimeBasedTriggerContext tc) {
        return "time-trigger-" + tc.getAt();
    }

    private LocalTime getNormalizedTime(LocalTime time) {
        return time.withNano(0);
    }
}