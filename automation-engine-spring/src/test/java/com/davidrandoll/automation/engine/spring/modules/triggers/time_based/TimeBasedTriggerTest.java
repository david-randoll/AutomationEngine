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
        verify(publisher).scheduleAt("time-trigger-10:00", time);
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
        verify(publisher).scheduleAt(anyString(), eq(time));
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
        verify(publisher).scheduleCron("cron-trigger-" + cron, cron);
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
        verify(publisher).scheduleCron(anyString(), eq(cron));
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
        verify(publisher).scheduleCron(anyString(), eq(cron));
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
        verify(publisher).scheduleCron(anyString(), eq(cron));
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

    @Test
    void shouldTriggerAtMidnight() {
        String cron = "0 0 0 * * *";
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 27, 0, 0, 0);
        TimeBasedEvent event = new TimeBasedEvent(dateTime.toLocalTime(), dateTime);
        when(eventContext.getEvent()).thenReturn(event);

        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        boolean result = trigger.isTriggered(eventContext, context);

        assertThat(result).isTrue();
        verify(publisher).scheduleCron(anyString(), eq(cron));
    }

    @Test
    void shouldTriggerEvery30Seconds() {
        String cron = "*/30 * * * * *";
        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        // At 0 seconds
        LocalDateTime dt0 = LocalDateTime.of(2023, 10, 27, 10, 0, 0);
        TimeBasedEvent event0 = new TimeBasedEvent(dt0.toLocalTime(), dt0);
        when(eventContext.getEvent()).thenReturn(event0);
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // At 30 seconds
        LocalDateTime dt30 = LocalDateTime.of(2023, 10, 27, 10, 0, 30);
        TimeBasedEvent event30 = new TimeBasedEvent(dt30.toLocalTime(), dt30);
        when(eventContext.getEvent()).thenReturn(event30);
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // At 15 seconds (should not trigger)
        LocalDateTime dt15 = LocalDateTime.of(2023, 10, 27, 10, 0, 15);
        TimeBasedEvent event15 = new TimeBasedEvent(dt15.toLocalTime(), dt15);
        when(eventContext.getEvent()).thenReturn(event15);
        assertThat(trigger.isTriggered(eventContext, context)).isFalse();
    }

    @Test
    void shouldTriggerOnceAWeek() {
        // Every Monday at 09:00:00
        String cron = "0 0 9 * * MON";
        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        // Monday, Oct 23, 2023
        LocalDateTime monday = LocalDateTime.of(2023, 10, 23, 9, 0, 0);
        TimeBasedEvent eventMonday = new TimeBasedEvent(monday.toLocalTime(), monday);
        when(eventContext.getEvent()).thenReturn(eventMonday);
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // Tuesday, Oct 24, 2023
        LocalDateTime tuesday = LocalDateTime.of(2023, 10, 24, 9, 0, 0);
        TimeBasedEvent eventTuesday = new TimeBasedEvent(tuesday.toLocalTime(), tuesday);
        when(eventContext.getEvent()).thenReturn(eventTuesday);
        assertThat(trigger.isTriggered(eventContext, context)).isFalse();
    }

    @Test
    void shouldTriggerOnLeapDay() {
        // Feb 29th at noon
        String cron = "0 0 12 29 2 *";
        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        // Feb 29, 2024 (Leap Year)
        LocalDateTime leapDay = LocalDateTime.of(2024, 2, 29, 12, 0, 0);
        TimeBasedEvent eventLeap = new TimeBasedEvent(leapDay.toLocalTime(), leapDay);
        when(eventContext.getEvent()).thenReturn(eventLeap);
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // Feb 28, 2024
        LocalDateTime feb28 = LocalDateTime.of(2024, 2, 28, 12, 0, 0);
        TimeBasedEvent eventFeb28 = new TimeBasedEvent(feb28.toLocalTime(), feb28);
        when(eventContext.getEvent()).thenReturn(eventFeb28);
        assertThat(trigger.isTriggered(eventContext, context)).isFalse();
    }

    @Test
    void shouldTriggerWithRangeAndList() {
        // 9 AM to 5 PM, Monday to Friday
        String cron = "0 0 9-17 * * MON-FRI";
        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        // Monday at 9 AM
        LocalDateTime monday9am = LocalDateTime.of(2023, 10, 23, 9, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(monday9am.toLocalTime(), monday9am));
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // Monday at 5 PM
        LocalDateTime monday5pm = LocalDateTime.of(2023, 10, 23, 17, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(monday5pm.toLocalTime(), monday5pm));
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // Monday at 6 PM (Out of range)
        LocalDateTime monday6pm = LocalDateTime.of(2023, 10, 23, 18, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(monday6pm.toLocalTime(), monday6pm));
        assertThat(trigger.isTriggered(eventContext, context)).isFalse();

        // Saturday at 9 AM (Weekend)
        LocalDateTime saturday9am = LocalDateTime.of(2023, 10, 28, 9, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(saturday9am.toLocalTime(), saturday9am));
        assertThat(trigger.isTriggered(eventContext, context)).isFalse();
    }

    @Test
    void shouldTriggerOnLastDayOfMonth() {
        // Midnight on the last day of the month
        String cron = "0 0 0 L * *";
        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        // Oct 31st
        LocalDateTime oct31 = LocalDateTime.of(2023, 10, 31, 0, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(oct31.toLocalTime(), oct31));
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // Oct 30th
        LocalDateTime oct30 = LocalDateTime.of(2023, 10, 30, 0, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(oct30.toLocalTime(), oct30));
        assertThat(trigger.isTriggered(eventContext, context)).isFalse();

        // Feb 29, 2024
        LocalDateTime feb29 = LocalDateTime.of(2024, 2, 29, 0, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(feb29.toLocalTime(), feb29));
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();
    }

    @Test
    void shouldTriggerEvery5Minutes() {
        String cron = "0 */5 * * * *";
        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        // At 10:00:00
        LocalDateTime dt0 = LocalDateTime.of(2023, 10, 27, 10, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(dt0.toLocalTime(), dt0));
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // At 10:05:00
        LocalDateTime dt5 = LocalDateTime.of(2023, 10, 27, 10, 5, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(dt5.toLocalTime(), dt5));
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // At 10:03:00 (should not trigger)
        LocalDateTime dt3 = LocalDateTime.of(2023, 10, 27, 10, 3, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(dt3.toLocalTime(), dt3));
        assertThat(trigger.isTriggered(eventContext, context)).isFalse();
    }

    @Test
    void shouldTriggerWithQuestionMark() {
        // 12:00:00 on the 1st of every month, regardless of day of week
        String cron = "0 0 12 1 * ?";
        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        // Jan 1st
        LocalDateTime jan1 = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(jan1.toLocalTime(), jan1));
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        // Jan 2nd
        LocalDateTime jan2 = LocalDateTime.of(2023, 1, 2, 12, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(jan2.toLocalTime(), jan2));
        assertThat(trigger.isTriggered(eventContext, context)).isFalse();
    }

    @Test
    void shouldTriggerEverySecond() {
        String cron = "* * * * * *";
        TimeBasedTriggerContext context = new TimeBasedTriggerContext();
        context.setCron(cron);

        LocalDateTime dt = LocalDateTime.of(2023, 10, 27, 10, 0, 0);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(dt.toLocalTime(), dt));
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();

        LocalDateTime dtNext = dt.plusSeconds(1);
        when(eventContext.getEvent()).thenReturn(new TimeBasedEvent(dtNext.toLocalTime(), dtNext));
        assertThat(trigger.isTriggered(eventContext, context)).isTrue();
    }
}
