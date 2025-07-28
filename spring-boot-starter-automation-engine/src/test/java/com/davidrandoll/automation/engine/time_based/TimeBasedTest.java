package com.davidrandoll.automation.engine.time_based;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class TimeBasedTest extends AutomationEngineTest {

    @Test
    void testThatAutomationTriggersAtSpecificTime() {
        var yaml = """
                alias: Yaml Automation Test
                triggers:
                  - trigger: time
                    at: 22:37
                actions:
                  - action: logger
                    message: Automation triggered at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var matchingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37, 0)));
        engine.publishEvent(matchingEvent);

        // Assert: Check that the automation triggered as expected
        assertThat(automation.anyTriggerActivated(matchingEvent))
                .as("Automation should trigger when the time matches")
                .isTrue();

        assertThat(automation.allConditionsMet(matchingEvent))
                .as("All conditions should be met for the automation")
                .isTrue();

        // Assert: Verify the log message
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered at 10:37 PM"));
    }

    @Test
    void testAutomationDoesNotTriggerForNonMatchingTime() {
        var yaml = """
                alias: Non-Matching Time Test
                triggers:
                  - trigger: time
                    at: 22:37
                actions:
                  - action: logger
                    message: Automation triggered at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var nonMatchingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0, 0)));
        engine.publishEvent(nonMatchingEvent);

        // Assert: Check that the automation did not trigger
        assertThat(automation.anyTriggerActivated(nonMatchingEvent))
                .as("Automation should not trigger when the time does not match")
                .isFalse();

        // Assert: Verify no log messages were produced
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Automation triggered at"));
    }

    @Test
    void testMultipleAutomationsTriggerIndependently() {
        var yaml1 = """
                alias: Morning Trigger Test
                triggers:
                  - trigger: time
                    at: 08:00
                actions:
                  - action: logger
                    message: Good morning! Automation triggered at {{ time | time_format(pattern='hh:mm a') }}
                """;

        var yaml2 = """
                alias: Evening Trigger Test
                triggers:
                  - trigger: time
                    at: 20:00
                actions:
                  - action: logger
                    message: Good evening! Automation triggered at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation morningAutomation = factory.createAutomation("yaml", yaml1);
        Automation eveningAutomation = factory.createAutomation("yaml", yaml2);
        engine.register(morningAutomation);
        engine.register(eveningAutomation);

        // Act: Trigger morning automation
        var morningEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(8, 0, 0)));
        engine.publishEvent(morningEvent);

        // Assert: Morning automation triggered
        assertThat(morningAutomation.anyTriggerActivated(morningEvent))
                .as("Morning automation should trigger at 08:00")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Good morning! Automation triggered at 08:00 AM"));

        // Act: Trigger evening automation
        var eveningEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(20, 0, 0)));
        engine.publishEvent(eveningEvent);

        // Assert: Evening automation triggered
        assertThat(eveningAutomation.anyTriggerActivated(eveningEvent))
                .as("Evening automation should trigger at 20:00")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Good evening! Automation triggered at 08:00 PM"));
    }

    @Test
    void testNoTriggerWithoutAutomationAdded() {
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37, 0));

        // Act: Process event without adding any automation
        engine.publishEvent(event);

        // Assert: No automations were triggered
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Automation triggered at"));
    }

    @Test
    void testAutomationWithTimeRangeCondition() {
        var yaml = """
                alias: Time Range Automation Test
                triggers:
                  - trigger: time
                    at: 22:37
                conditions:
                  - condition: time
                    after: 22:30
                    before: 23:00
                actions:
                  - action: logger
                    message: Automation triggered at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events at different times
        var withinRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37)));
        var beforeRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 20)));
        var afterRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(23, 5)));

        // Process events
        engine.publishEvent(withinRangeEvent);
        engine.publishEvent(beforeRangeEvent);
        engine.publishEvent(afterRangeEvent);

        // Assert: Automation should only trigger for the within-range event
        assertThat(automation.anyTriggerActivated(withinRangeEvent))
                .as("Automation should trigger within the time range")
                .isTrue();

        assertThat(automation.allConditionsMet(withinRangeEvent))
                .as("Conditions should be met within the time range")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered at 10:37 PM"));

        // Assert: Automation should not trigger for events outside the range
        assertThat(automation.anyTriggerActivated(beforeRangeEvent))
                .as("Automation should not trigger before the time range")
                .isFalse();

        assertThat(automation.allConditionsMet(beforeRangeEvent))
                .as("Conditions should not be met before the time range")
                .isFalse();

        assertThat(automation.anyTriggerActivated(afterRangeEvent))
                .as("Automation should not trigger after the time range")
                .isFalse();

        assertThat(automation.allConditionsMet(afterRangeEvent))
                .as("Conditions should not be met after the time range")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .as("Automation not triggered for events outside the time range")
                .noneMatch(msg -> msg.contains("Automation triggered at 10:20 PM"))
                .noneMatch(msg -> msg.contains("Automation triggered at 11:05 PM"));
    }
}