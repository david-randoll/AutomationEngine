package com.davidrandoll.automation.engine.spring.modules.triggers.time_based;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeBasedTriggerTest {

    @Mock
    private TimeBasedEventPublisher publisher;

    @Mock
    private EventContext eventContext;

    private TimeBasedTrigger trigger;

    @BeforeEach
    void setUp() {
        trigger = new TimeBasedTrigger(publisher);
    }

    @Test
    void shouldNotTriggerWhenEventIsNotTimeBasedEvent() {
        IEvent otherEvent = mock(IEvent.class);
        when(eventContext.getEvent()).thenReturn(otherEvent);
        TimeBasedTriggerContext context = new TimeBasedTriggerContext();

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isFalse();
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldTriggerWhenAtMatchesTime() {
        LocalTime time = LocalTime.of(10, 0);
        TimeBasedEvent event = new TimeBasedEvent(time, LocalDateTime.now().with(time));
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setAt(time);
        context.setAlias("test-trigger");

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isTrue();
        verify(publisher).scheduleAt("test-trigger", time);
    }

    @Test
    void shouldTriggerWhenAtMatchesTimeWithDifferentNanos() {
        LocalTime time = LocalTime.of(10, 0, 0, 0);
        LocalTime eventTime = LocalTime.of(10, 0, 0, 500);
        TimeBasedEvent event = new TimeBasedEvent(eventTime, LocalDateTime.now().with(eventTime));
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setAt(time);

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isTrue();
        verify(publisher).scheduleAt(eq("time-trigger-10:00"), eq(time));
    }

    @Test
    void shouldNotTriggerWhenAtDoesNotMatchTime() {
        LocalTime time = LocalTime.of(10, 0);
        LocalTime eventTime = LocalTime.of(11, 0);
        TimeBasedEvent event = new TimeBasedEvent(eventTime, LocalDateTime.now().with(eventTime));
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setAt(time);

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isFalse();
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldTriggerWhenCronMatchesTime() {
        // Every day at 10:00:00
        String cron = "0 0 10 * * *";
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 27, 10, 0, 0);
        TimeBasedEvent event = new TimeBasedEvent(dateTime.toLocalTime(), dateTime);
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);
        context.setAlias("cron-trigger");

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isTrue();
        verify(publisher).scheduleCron("cron-trigger", cron);
    }

    @Test
    void shouldTriggerWhenCronMatchesTimeWithDifferentNanos() {
        String cron = "0 0 10 * * *";
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 27, 10, 0, 0, 500);
        TimeBasedEvent event = new TimeBasedEvent(dateTime.toLocalTime(), dateTime);
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isTrue();
        verify(publisher).scheduleCron("cron-trigger-" + cron, cron);
    }

    @Test
    void shouldNotTriggerWhenCronDoesNotMatchTime() {
        String cron = "0 0 10 * * *";
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 27, 11, 0, 0);
        TimeBasedEvent event = new TimeBasedEvent(dateTime.toLocalTime(), dateTime);
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isFalse();
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldNotTriggerWhenCronIsInvalid() {
        String cron = "invalid-cron";
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 27, 10, 0, 0);
        TimeBasedEvent event = new TimeBasedEvent(dateTime.toLocalTime(), dateTime);
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isFalse();
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldNotTriggerWhenCronMatchesButDateTimeIsNull() {
        String cron = "0 0 10 * * *";
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0), null);
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isFalse();
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldHandleNullPublisher() {
        trigger = new TimeBasedTrigger(null);
        LocalTime time = LocalTime.of(10, 0);
        TimeBasedEvent event = new TimeBasedEvent(time, LocalDateTime.now().with(time));
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setAt(time);

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isTrue();
        // No exception should be thrown
    }
}
