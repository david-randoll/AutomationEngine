package com.davidrandoll.automation.engine.conditions;

import com.davidrandoll.automation.engine.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class AndConditionTest extends AutomationEngineTest {

    @Test
    void testAutomationWithTimeRangeCondition() {
        var yaml = """
                alias: And Condition Automation Test
                triggers:
                  - trigger: time
                    at: 22:37
                conditions:
                  - condition: and
                    conditions:
                      - condition: time
                        after: 22:30
                      - condition: time
                        before: 23:00
                actions:
                  - action: logger
                    message: Automation triggered with all conditions met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events at different times
        EventContext withinRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37)));
        EventContext beforeRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 20)));
        EventContext afterRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(23, 5)));

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
                .anyMatch(msg -> msg.contains("Automation triggered with all conditions met at 10:37 PM"));

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
                .noneMatch(msg -> msg.contains("Automation triggered with all conditions met at 10:20 PM"))
                .noneMatch(msg -> msg.contains("Automation triggered with all conditions met at 11:05 PM"));
    }

    @Test
    void testAutomationWithExactBoundaryTimes() {
        var yaml = """
                alias: And Condition Boundary Test
                triggers:
                  - trigger: time
                    at: 22:30
                  - trigger: time
                    at: 23:00
                conditions:
                  - condition: and
                    conditions:
                      - condition: time
                        after: 22:30
                      - condition: time
                        before: 23:00
                actions:
                  - action: logger
                    message: Automation triggered with all conditions met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events exactly at boundary times
        EventContext onBoundaryBefore = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 30))); // On the "after 22:30" boundary
        EventContext onBoundaryAfter = EventContext.of(new TimeBasedEvent(LocalTime.of(23, 0))); // On the "before 23:00" boundary

        // Process events
        engine.publishEvent(onBoundaryBefore);
        engine.publishEvent(onBoundaryAfter);

        // Assert: Automation should trigger when on the boundary
        assertThat(automation.anyTriggerActivated(onBoundaryBefore))
                .as("Automation should trigger at 22:30 as it satisfies 'after 22:30' condition")
                .isTrue();

        assertThat(automation.allConditionsMet(onBoundaryBefore))
                .as("Conditions should not met at 22:30")
                .isFalse();

        assertThat(automation.anyTriggerActivated(onBoundaryAfter))
                .as("Automation should trigger at 23:00 as it satisfies 'before 23:00' condition")
                .isTrue();

        assertThat(automation.allConditionsMet(onBoundaryAfter))
                .as("Conditions should not be met at 23:00")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Automation triggered with all conditions met at 10:30 PM"))
                .noneMatch(msg -> msg.contains("Automation triggered with all conditions met at 11:00 PM"));
    }

    @Test
    void testAutomationWithConditionFailingInAndBlock() {
        var yaml = """
                alias: And Condition Failing Test
                triggers:
                  - trigger: time
                    at: 22:37
                  - trigger: time
                    at: 22:33
                conditions:
                  - condition: and
                    conditions:
                      - condition: time
                        after: 22:30
                      - condition: time
                        before: 22:35
                actions:
                  - action: logger
                    message: Automation triggered with all conditions met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events that fail the condition
        EventContext failingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37))); // Fails the "before 22:35" condition
        EventContext passingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 33))); // Meets both conditions

        // Process events
        engine.publishEvent(failingEvent);
        engine.publishEvent(passingEvent);

        // Assert: Automation should fail when one condition is violated
        assertThat(automation.anyTriggerActivated(failingEvent))
                .as("Automation should trigger when time is 22:37")
                .isTrue();

        assertThat(automation.allConditionsMet(failingEvent))
                .as("Conditions should not be met when the time is after 22:35")
                .isFalse();

        // Assert: Automation should trigger when all conditions are met
        assertThat(automation.anyTriggerActivated(passingEvent))
                .as("Automation should trigger when time is 22:33")
                .isTrue();

        assertThat(automation.allConditionsMet(passingEvent))
                .as("Conditions should be met when time is between 22:30 and 22:35")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered with all conditions met at 10:33 PM"));
    }

    @Test
    void testAutomationWithMultipleTimeConditions() {
        var yaml = """
                alias: And Condition Multiple Time Test
                triggers:
                  - trigger: time
                    at: 22:37
                conditions:
                  - condition: and
                    conditions:
                      - condition: time
                        after: 22:30
                      - condition: time
                        before: 22:45
                actions:
                  - action: logger
                    message: Automation triggered with all conditions met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events at various times
        EventContext withinRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37))); // Between 22:30 and 22:45
        EventContext outsideRangeBefore = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 20))); // Before 22:30
        EventContext outsideRangeAfter = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 50))); // After 22:45

        // Process events
        engine.publishEvent(withinRangeEvent);
        engine.publishEvent(outsideRangeBefore);
        engine.publishEvent(outsideRangeAfter);

        // Assert: Automation should only trigger for the within-range event
        assertThat(automation.anyTriggerActivated(withinRangeEvent))
                .as("Automation should trigger between 22:30 and 22:45")
                .isTrue();

        assertThat(automation.allConditionsMet(withinRangeEvent))
                .as("Conditions should be met for 22:37 event")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered with all conditions met at 10:37 PM"));

        // Assert: Automation should not trigger for events outside the range
        assertThat(automation.anyTriggerActivated(outsideRangeBefore))
                .as("Automation should not trigger before 22:30")
                .isFalse();

        assertThat(automation.allConditionsMet(outsideRangeBefore))
                .as("Conditions should not be met before 22:30")
                .isFalse();

        assertThat(automation.anyTriggerActivated(outsideRangeAfter))
                .as("Automation should not trigger after 22:45")
                .isFalse();

        assertThat(automation.allConditionsMet(outsideRangeAfter))
                .as("Conditions should not be met after 22:45")
                .isFalse();
    }

    @Test
    void testAutomationWithBoundaryFailingCondition() {
        var yaml = """
                alias: And Condition Boundary Failing Test
                triggers:
                  - trigger: time
                    at: 22:35
                  - trigger: time
                    at: 22:34
                conditions:
                  - condition: and
                    conditions:
                      - condition: time
                        after: 22:30
                      - condition: time
                        before: 22:35
                actions:
                  - action: logger
                    message: Automation triggered with all conditions met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events at boundary times
        EventContext failingBoundaryEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 35))); // On the "before 22:35" boundary
        EventContext passingBoundaryEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 34))); // Before 22:35

        // Process events
        engine.publishEvent(failingBoundaryEvent);
        engine.publishEvent(passingBoundaryEvent);

        // Assert: Automation should fail when boundary is exactly on the failed condition
        assertThat(automation.anyTriggerActivated(failingBoundaryEvent))
                .as("Automation should trigger when time is exactly 22:35")
                .isTrue();

        assertThat(automation.allConditionsMet(failingBoundaryEvent))
                .as("Conditions should not be met at 22:35")
                .isFalse();

        // Assert: Automation should trigger when the time is strictly within range
        assertThat(automation.anyTriggerActivated(passingBoundaryEvent))
                .as("Automation should trigger when time is before 22:35")
                .isTrue();

        assertThat(automation.allConditionsMet(passingBoundaryEvent))
                .as("Conditions should be met before 22:35")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered with all conditions met at 10:34 PM"));
    }

    @Test
    void testAutomationWithNestedAndConditions() {
        var yaml = """
                alias: Nested And Condition Test
                triggers:
                  - trigger: time
                    at: 22:40
                conditions:
                  - condition: and
                    conditions:
                      - condition: and
                        conditions:
                          - condition: time
                            after: 22:30
                          - condition: time
                            before: 22:50
                      - condition: and
                        conditions:
                          - condition: time
                            after: 22:35
                          - condition: time
                            before: 22:45
                actions:
                  - action: logger
                    message: Automation triggered with nested conditions met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events to test nested conditions
        EventContext matchingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 40))); // Satisfies both nested conditions
        EventContext failingFirstAndCondition = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 55))); // Fails first AND condition
        EventContext failingSecondAndCondition = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 33))); // Fails second AND condition

        // Process events
        engine.publishEvent(matchingEvent);
        engine.publishEvent(failingFirstAndCondition);
        engine.publishEvent(failingSecondAndCondition);

        // Assert: Only the matching event should trigger the automation
        assertThat(automation.anyTriggerActivated(matchingEvent))
                .as("Automation should trigger when time is 22:40")
                .isTrue();

        assertThat(automation.allConditionsMet(matchingEvent))
                .as("Conditions should be met when time is 22:40")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered with nested conditions met at 10:40 PM"));

        // Assert: Events failing either AND condition should not trigger
        assertThat(automation.anyTriggerActivated(failingFirstAndCondition))
                .as("Automation should not trigger at 22:55 since it fails first AND condition")
                .isFalse();

        assertThat(automation.allConditionsMet(failingFirstAndCondition))
                .as("Conditions should not be met at 22:55")
                .isFalse();

        assertThat(automation.anyTriggerActivated(failingSecondAndCondition))
                .as("Automation should not trigger at 22:33 since it fails second AND condition")
                .isFalse();

        assertThat(automation.allConditionsMet(failingSecondAndCondition))
                .as("Conditions should not be met at 22:33")
                .isFalse();
    }

    @Test
    void testAutomationWithDeepNestedAndConditions() {
        var yaml = """
                alias: Deep Nested And Condition Test
                triggers:
                  - trigger: time
                    at: 22:42
                conditions:
                  - condition: and
                    conditions:
                      - condition: and
                        conditions:
                          - condition: and
                            conditions:
                              - condition: time
                                after: 22:30
                              - condition: time
                                before: 22:50
                          - condition: and
                            conditions:
                              - condition: time
                                after: 22:35
                              - condition: time
                                before: 22:45
                      - condition: and
                        conditions:
                          - condition: and
                            conditions:
                              - condition: time
                                after: 22:38
                              - condition: time
                                before: 22:44
                actions:
                  - action: logger
                    message: Automation triggered with deep nested conditions met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events to test deep nested conditions
        EventContext matchingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 42))); // Satisfies all nested conditions
        EventContext failingFirstLayer = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 55))); // Fails first AND condition
        EventContext failingSecondLayer = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 33))); // Fails second AND condition
        EventContext failingThirdLayer = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37))); // Fails third AND condition

        // Process events
        engine.publishEvent(matchingEvent);
        engine.publishEvent(failingFirstLayer);
        engine.publishEvent(failingSecondLayer);
        engine.publishEvent(failingThirdLayer);

        // Assert: Only the matching event should trigger the automation
        assertThat(automation.anyTriggerActivated(matchingEvent))
                .as("Automation should trigger when time is 22:42")
                .isTrue();

        assertThat(automation.allConditionsMet(matchingEvent))
                .as("Conditions should be met when time is 22:42")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered with deep nested conditions met at 10:42 PM"));

        // Assert: Events failing any AND condition should not trigger
        assertThat(automation.anyTriggerActivated(failingFirstLayer))
                .as("Automation should not trigger at 22:55 since it fails first AND condition")
                .isFalse();

        assertThat(automation.allConditionsMet(failingFirstLayer))
                .as("Conditions should not be met at 22:55")
                .isFalse();

        assertThat(automation.anyTriggerActivated(failingSecondLayer))
                .as("Automation should not trigger at 22:33 since it fails second AND condition")
                .isFalse();

        assertThat(automation.allConditionsMet(failingSecondLayer))
                .as("Conditions should not be met at 22:33")
                .isFalse();

        assertThat(automation.anyTriggerActivated(failingThirdLayer))
                .as("Automation should not trigger at 22:37 since it fails third AND condition")
                .isFalse();

        assertThat(automation.allConditionsMet(failingThirdLayer))
                .as("Conditions should not be met at 22:37")
                .isFalse();
    }
}
