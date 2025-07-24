package com.davidrandoll.automation.engine.conditions;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrConditionTest extends AutomationEngineTest {

    @Test
    void testAutomationWithTimeRangeCondition() {
        var yaml = """
                alias: Or Condition Automation Test
                triggers:
                  - trigger: time
                    at: 22:37
                conditions:
                  - condition: or
                    conditions:
                      - condition: time
                        after: 22:30
                      - condition: time
                        before: 23:00
                actions:
                  - action: logger
                    message: Automation triggered when one condition was met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events at different times
        var withinRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37))); // Should trigger
        var beforeRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 20))); // Should not trigger
        var afterRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(23, 5)));   // Should not trigger

        // Process events
        engine.publishEvent(withinRangeEvent);
        engine.publishEvent(beforeRangeEvent);
        engine.publishEvent(afterRangeEvent);

        // Assert: Automation should trigger for events that meet any condition
        assertThat(automation.anyTriggerActivated(withinRangeEvent))
                .as("Automation should trigger at 22:37")
                .isTrue();
        assertThat(automation.allConditionsMet(withinRangeEvent))
                .as("Conditions should be met for 22:37")
                .isTrue();

        assertThat(automation.anyTriggerActivated(beforeRangeEvent))
                .as("Automation should not trigger at 22:20 (does not match conditions)")
                .isFalse();
        assertThat(automation.allConditionsMet(beforeRangeEvent))
                .as("Conditions should met for 22:20")
                .isTrue();

        assertThat(automation.anyTriggerActivated(afterRangeEvent))
                .as("Automation should not trigger after the time range")
                .isFalse();
        assertThat(automation.allConditionsMet(afterRangeEvent))
                .as("Conditions should met for 23:05")
                .isTrue();

        // Assert: Logged messages
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered when one condition was met at 10:37 PM"))
                .noneMatch(msg -> msg.contains("Automation triggered when one condition was met at 10:20 PM"))
                .noneMatch(msg -> msg.contains("Automation triggered when one condition was met at 11:05 PM"));
    }

    @Test
    void testAutomationWithExactBoundaryTimes() {
        var yaml = """
                alias: Or Condition Boundary Test
                triggers:
                  - trigger: time
                    at: 22:30
                  - trigger: time
                    at: 23:00
                conditions:
                  - condition: or
                    conditions:
                      - condition: time
                        after: 22:30
                      - condition: time
                        before: 23:00
                actions:
                  - action: logger
                    message: Automation triggered when one condition was met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events exactly at boundary times
        var onBoundaryBefore = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 30))); // On the "after 22:30" boundary
        var onBoundaryAfter = EventContext.of(new TimeBasedEvent(LocalTime.of(23, 0))); // On the "before 23:00" boundary

        // Process events
        engine.publishEvent(onBoundaryBefore);
        engine.publishEvent(onBoundaryAfter);

        // Assert: Automation should trigger when on the boundary
        assertThat(automation.anyTriggerActivated(onBoundaryBefore))
                .as("Automation should trigger at 22:30 as it satisfies 'after 22:30' condition")
                .isTrue();

        assertThat(automation.allConditionsMet(onBoundaryBefore))
                .as("Conditions should be met at 22:30")
                .isTrue();

        assertThat(automation.anyTriggerActivated(onBoundaryAfter))
                .as("Automation should trigger at 23:00 as it satisfies 'before 23:00' condition")
                .isTrue();

        assertThat(automation.allConditionsMet(onBoundaryAfter))
                .as("Conditions should be met at 23:00")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered when one condition was met at 10:30 PM"))
                .anyMatch(msg -> msg.contains("Automation triggered when one condition was met at 11:00 PM"));
    }

    @Test
    void testAutomationWithConditionFailingInOrBlock() {
        var yaml = """
                alias: Or Condition Failing Test
                triggers:
                  - trigger: time
                    at: 22:37
                  - trigger: time
                    at: 22:33
                conditions:
                  - condition: or
                    conditions:
                      - condition: time
                        after: 22:30
                      - condition: time
                        before: 22:35
                actions:
                  - action: logger
                    message: Automation triggered when one condition was met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events that fail and pass the condition
        var failingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37))); // Fails "before 22:35" but passes "after 22:30"
        var passingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 33))); // Meets "after 22:30" and "before 22:35"

        // Process events
        engine.publishEvent(failingEvent);
        engine.publishEvent(passingEvent);

        // Assert: Automation should trigger for both events due to 'or' condition
        assertThat(automation.anyTriggerActivated(failingEvent))
                .as("Automation should trigger when time is 22:37 as it satisfies 'after 22:30' condition")
                .isTrue();

        assertThat(automation.allConditionsMet(failingEvent))
                .as("Conditions should be met when at least one condition is satisfied")
                .isTrue();

        assertThat(automation.anyTriggerActivated(passingEvent))
                .as("Automation should trigger when time is 22:33 as it satisfies 'or' condition")
                .isTrue();

        assertThat(automation.allConditionsMet(passingEvent))
                .as("Conditions should be met when time is between 22:30 and 22:35")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered when one condition was met at 10:33 PM"))
                .anyMatch(msg -> msg.contains("Automation triggered when one condition was met at 10:37 PM"));
    }

    @Test
    void testAutomationWithMultipleTimeConditions() {
        var yaml = """
                alias: And Condition Multiple Time Test
                triggers:
                  - trigger: time
                    at: 22:37
                conditions:
                  - condition: or
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
        var withinRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37))); // Between 22:30 and 22:45
        var outsideRangeBefore = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 20))); // Before 22:30
        var outsideRangeAfter = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 50))); // After 22:45

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
                .as("Conditions should be met before 22:30")
                .isTrue();

        assertThat(automation.anyTriggerActivated(outsideRangeAfter))
                .as("Automation should not trigger after 22:45")
                .isFalse();

        assertThat(automation.allConditionsMet(outsideRangeAfter))
                .as("Conditions should be met after 22:45")
                .isTrue();
    }

    @Test
    void testAutomationWithBoundaryFailingCondition() {
        var yaml = """
                alias: Boundary Failing Condition Test
                triggers:
                  - trigger: time
                    at: 22:35
                  - trigger: time
                    at: 22:34
                conditions:
                  - condition: or
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
        var boundaryFailingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 35))); // Fails at boundary
        var boundaryPassingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 34))); // Passes within range

        // Process events
        engine.publishEvent(boundaryFailingEvent);
        engine.publishEvent(boundaryPassingEvent);

        // Assert: Automation should not activate when failing boundary is reached
        assertThat(automation.anyTriggerActivated(boundaryFailingEvent))
                .as("Automation should trigger at 22:35")
                .isTrue();

        assertThat(automation.allConditionsMet(boundaryFailingEvent))
                .as("Conditions should be met at 22:35")
                .isTrue();

        // Assert: Automation should activate when within the range
        assertThat(automation.anyTriggerActivated(boundaryPassingEvent))
                .as("Automation should trigger when time is before 22:35")
                .isTrue();

        assertThat(automation.allConditionsMet(boundaryPassingEvent))
                .as("Conditions should be met before 22:35")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered with all conditions met at 10:34 PM"));
    }

    @Test
    void testAutomationWithNestedOrConditions() {
        var yaml = """
                alias: Nested Or Condition Test
                triggers:
                  - trigger: time
                    at: 22:40
                conditions:
                  - condition: or
                    conditions:
                      - condition: or
                        conditions:
                          - condition: time
                            after: 22:30
                          - condition: time
                            before: 22:50
                      - condition: or
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
        var validEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 40))); // Satisfies at least one OR condition
        var eventOutsideRange1 = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 55))); // Fails all OR conditions
        var eventOutsideRange2 = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 33))); // Fails all OR conditions

        // Process events
        engine.publishEvent(validEvent);
        engine.publishEvent(eventOutsideRange1);
        engine.publishEvent(eventOutsideRange2);

        // Assert: Only the valid event should trigger the automation
        assertThat(automation.anyTriggerActivated(validEvent))
                .as("Automation should trigger when time is 22:40")
                .isTrue();

        assertThat(automation.allConditionsMet(validEvent))
                .as("Conditions should be met when time is 22:40")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered with nested conditions met at 10:40 PM"));

        // Assert: Events outside the defined OR conditions should not trigger
        assertThat(automation.anyTriggerActivated(eventOutsideRange1))
                .as("Automation should not trigger at 22:55 since it fails all OR conditions")
                .isFalse();

        assertThat(automation.allConditionsMet(eventOutsideRange1))
                .as("Conditions should be met at 22:55")
                .isTrue();

        assertThat(automation.anyTriggerActivated(eventOutsideRange2))
                .as("Automation should not trigger at 22:33 since it fails all OR conditions")
                .isFalse();

        assertThat(automation.allConditionsMet(eventOutsideRange2))
                .as("Conditions should be met at 22:33")
                .isTrue();
    }

    @Test
    void testAutomationWithDeepNestedAndConditions() {
        var yaml = """
                alias: Deep Nested And Condition Test
                triggers:
                  - trigger: time
                    at: 22:42
                conditions:
                  - condition: or
                    conditions:
                      - condition: or
                        conditions:
                          - condition: or
                            conditions:
                              - condition: time
                                after: 22:30
                              - condition: time
                                before: 22:50
                          - condition: or
                            conditions:
                              - condition: time
                                after: 22:35
                              - condition: time
                                before: 22:45
                      - condition: or
                        conditions:
                          - condition: or
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
        var matchingEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 42))); // Satisfies all nested conditions
        var failingFirstLayer = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 55))); // Fails first AND condition
        var failingSecondLayer = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 33))); // Fails second AND condition
        var failingThirdLayer = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 37))); // Fails third AND condition

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
                .as("Conditions should be met at 22:55")
                .isTrue();

        assertThat(automation.anyTriggerActivated(failingSecondLayer))
                .as("Automation should not trigger at 22:33 since it fails second AND condition")
                .isFalse();

        assertThat(automation.allConditionsMet(failingSecondLayer))
                .as("Conditions should be met at 22:33")
                .isTrue();

        assertThat(automation.anyTriggerActivated(failingThirdLayer))
                .as("Automation should not trigger at 22:37 since it fails third AND condition")
                .isFalse();

        assertThat(automation.allConditionsMet(failingThirdLayer))
                .as("Conditions should be met at 22:37")
                .isTrue();
    }
}