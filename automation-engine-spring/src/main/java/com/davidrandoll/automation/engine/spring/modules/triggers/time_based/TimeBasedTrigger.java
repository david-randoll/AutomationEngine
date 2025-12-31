package com.davidrandoll.automation.engine.spring.modules.triggers.time_based;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEventPublisher;
import com.davidrandoll.automation.engine.spring.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Trigger that activates at a specific time of day.
 *
 * <p>
 * When this trigger is evaluated with a matching time, it automatically
 * schedules the next occurrence with the TimeBasedEventPublisher.
 */
@Slf4j
@RequiredArgsConstructor
public class TimeBasedTrigger extends PluggableTrigger<TimeBasedTriggerContext> {
    private final TimeBasedEventPublisher publisher;

    @Override
    public boolean isTriggered(EventContext ec, TimeBasedTriggerContext tc) {
        if (!(ec.getEvent() instanceof TimeBasedEvent timeBasedEvent))
            return false;

        boolean matches = false;

        if (tc.getCron() != null) {
            matches = evaluateCron(timeBasedEvent, tc.getCron());

            // Schedule the next occurrence
            if (publisher != null) {
                String scheduleKey = getScheduleKey(tc);
                publisher.scheduleCron(scheduleKey, tc.getCron());
            }
        } else if (tc.getAt() != null) {
            LocalTime eventTime = timeBasedEvent.getTime();
            LocalTime atTime = tc.getAt();

            matches = getNormalizedTime(eventTime).equals(getNormalizedTime(atTime));

            // Schedule the next occurrence
            if (publisher != null) {
                String scheduleKey = getScheduleKey(tc);
                publisher.scheduleAt(scheduleKey, atTime);
            }
        }

        return matches;
    }

    private boolean evaluateCron(TimeBasedEvent event, String cronExpression) {
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            LocalDateTime dateTime = event.getDateTime();
            if (dateTime == null)
                return false;

            // Check if the event's dateTime matches the cron expression.
            // Since the event is fired AT the time, we check if the next execution after
            // (dateTime - 1s) is dateTime.
            LocalDateTime next = cron.next(dateTime.minusSeconds(1));
            return next != null && getNormalizedDateTime(next).equals(getNormalizedDateTime(dateTime));
        } catch (Exception e) {
            log.error("Failed to evaluate cron expression: {}", cronExpression, e);
            return false;
        }
    }

    /**
     * Creates a unique schedule key from the trigger context.
     * Uses the trigger alias if available, otherwise generates from time or cron.
     */
    private String getScheduleKey(TimeBasedTriggerContext tc) {
        if (tc.getCron() != null) {
            return "cron-trigger-" + tc.getCron();
        }

        return "time-trigger-" + tc.getAt();
    }

    private LocalTime getNormalizedTime(LocalTime time) {
        return time.withNano(0);
    }

    private LocalDateTime getNormalizedDateTime(LocalDateTime dt) {
        return dt.withNano(0);
    }
}