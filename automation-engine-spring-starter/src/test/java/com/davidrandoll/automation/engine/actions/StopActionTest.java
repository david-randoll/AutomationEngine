package com.davidrandoll.automation.engine.actions;

import com.davidrandoll.automation.engine.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class StopActionTest extends AutomationEngineTest {

    @Test
    void testStopActionSequenceWhenConditionIsMet() {
        var yaml = """
                alias: Stop Sequence When Condition Is Met
                triggers:
                  - trigger: time
                    at: 22:30
                actions:
                  - action: logger
                    message: "Action before stop"
                  - action: stop
                    stopActionSequence: true
                    condition:
                      condition: time
                      after: 22:00
                  - action: logger
                    message: "This should not be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that satisfies stop condition (after 10 PM)
        TimeBasedEvent eventAfter10PM = new TimeBasedEvent(LocalTime.of(22, 30));

        // Process event
        engine.publishEvent(eventAfter10PM);

        // Assert: The first log message should be logged, but the second should not
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Action before stop"))
                .noneMatch(msg -> msg.contains("This should not be logged"));
    }

    @Test
    void testContinueActionSequenceWhenConditionIsNotMet() {
        var yaml = """
                alias: Continue Sequence When Condition Is Not Met
                triggers:
                  - trigger: time
                    at: 21:30
                actions:
                  - action: logger
                    message: "Action before stop"
                  - action: stop
                    stopActionSequence: true
                    condition:
                      condition: time
                      after: 22:00
                  - action: logger
                    message: "This should be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that does not satisfy stop condition (before 10 PM)
        TimeBasedEvent eventBefore10PM = new TimeBasedEvent(LocalTime.of(21, 30));

        // Process event
        engine.publishEvent(eventBefore10PM);

        // Assert: Both log messages should be logged
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Action before stop"))
                .anyMatch(msg -> msg.contains("This should be logged"));
    }

    @Test
    void testStopActionSequenceWithNoCondition() {
        var yaml = """
                alias: Stop Action Sequence With No Condition
                triggers:
                  - trigger: time
                    at: 20:00
                actions:
                  - action: logger
                    message: "Action before stop"
                  - action: stop
                  - action: logger
                    message: "This should be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create an event with no condition set
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(20, 0));

        // Process event
        engine.publishEvent(event);

        // Assert: Stop action has no condition, so execution should continue
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Action before stop"))
                .anyMatch(msg -> msg.contains("This should be logged"));
    }

    @Test
    void testStopActionSequenceInMiddle() {
        var yaml = """
                alias: Stop Action Sequence In Middle
                triggers:
                  - trigger: time
                    at: 12:00
                actions:
                  - action: logger
                    message: "Start sequence"
                  - action: stop
                    stopActionSequence: true
                    condition:
                      condition: alwaysTrue
                  - action: logger
                    message: "This should not be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create a time event at 12:00
        TimeBasedEvent eventAtNoon = new TimeBasedEvent(LocalTime.of(12, 0));

        // Process event
        engine.publishEvent(eventAtNoon);

        // Assert: The first log message should be logged, but the second should not
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Start sequence"))
                .noneMatch(msg -> msg.contains("This should not be logged"));
    }


    @Test
    void testStopActionSequenceAsFirstAction() {
        var yaml = """
                alias: Stop Action Sequence As First Action
                triggers:
                  - trigger: time
                    at: 08:00
                actions:
                  - action: stop
                    stopActionSequence: true
                    condition:
                      condition: alwaysTrue
                  - action: logger
                    message: "This should not be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create a time event at 08:00
        TimeBasedEvent eventAt8AM = new TimeBasedEvent(LocalTime.of(8, 0));

        // Process event
        engine.publishEvent(eventAt8AM);

        // Assert: No actions should be logged since the sequence is stopped immediately
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not be logged"));
    }

    @Test
    void testStopAutomationAffectsOnlyCurrentAutomation() {
        var yaml1 = """
                alias: Automation 1
                triggers:
                  - trigger: time
                    at: 10:00
                actions:
                  - action: logger
                    message: "Automation 1 before stop"
                  - action: stop
                    condition:
                      condition: alwaysTrue
                    stopAutomation: true
                  - action: logger
                    message: "This should not be logged in Automation 1"
                """;

        var yaml2 = """
                alias: Automation 2
                triggers:
                  - trigger: time
                    at: 10:00
                actions:
                  - action: logger
                    message: "Automation 2 running"
                """;

        Automation automation1 = factory.createAutomation("yaml", yaml1);
        Automation automation2 = factory.createAutomation("yaml", yaml2);
        engine.register(automation1);
        engine.register(automation2);

        // Act: Create a time event at 10:00
        TimeBasedEvent eventAt10AM = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(eventAt10AM);

        // Assert: Automation 1 should stop after the stop action, but Automation 2 should continue
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation 1 before stop"))
                .noneMatch(msg -> msg.contains("This should not be logged in Automation 1"))
                .anyMatch(msg -> msg.contains("Automation 2 running"));
    }

    @Test
    void testStopAutomationWithoutAffectingOthers() {
        var yaml1 = """
                alias: Stop-Affected Automation
                triggers:
                  - trigger: time
                    at: 15:00
                actions:
                  - action: logger
                    message: "Stopping automation now"
                  - action: stop
                    condition:
                      condition: alwaysTrue
                    stopAutomation: true
                  - action: logger
                    message: "This should never be logged"
                """;

        var yaml2 = """
                alias: Unaffected Automation
                triggers:
                  - trigger: time
                    at: 15:00
                actions:
                  - action: logger
                    message: "Unaffected Automation is running"
                """;

        Automation stopAffected = factory.createAutomation("yaml", yaml1);
        Automation unaffected = factory.createAutomation("yaml", yaml2);
        engine.register(stopAffected);
        engine.register(unaffected);

        // Act: Create a time event at 15:00
        TimeBasedEvent eventAt3PM = new TimeBasedEvent(LocalTime.of(15, 0));
        engine.publishEvent(eventAt3PM);

        // Assert: The first automation should stop, the second should proceed
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Stopping automation now"))
                .noneMatch(msg -> msg.contains("This should never be logged"))
                .anyMatch(msg -> msg.contains("Unaffected Automation is running"));
    }

    @Test
    void testStopAutomationVsStopActionSequence() {
        var yaml = """
                alias: Stop Automation vs Stop Action Sequence
                triggers:
                  - trigger: time
                    at: 10:00
                actions:
                  - action: logger
                    message: "Start sequence"
                
                  # First IF block with stopAutomation
                  - action: ifThenElse
                    if:
                      - condition: time
                        before: 11:00
                    then:
                      - action: logger
                        message: "Before stopping automation"
                      - action: stop
                        condition:
                          condition: alwaysTrue
                        stopAutomation: true
                      - action: logger
                        message: "This should never be logged"
                
                  # Second IF block with stopActionSequence
                  - action: ifThenElse
                    if:
                      - condition: time
                        before: 11:00
                    then:
                      - action: logger
                        message: "Before stopping sequence"
                      - action: stop
                        condition:
                          condition: alwaysTrue
                        stopActionSequence: true
                      - action: logger
                        message: "This should not be logged inside IF block"
                
                  # Actions outside IF should still execute after stopActionSequence
                  - action: logger
                    message: "Final log after sequence stop"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that meets both stop conditions
        TimeBasedEvent eventAt10AM = new TimeBasedEvent(LocalTime.of(10, 0));

        // Process event
        engine.publishEvent(eventAt10AM);

        // Assert:
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Start sequence"))  // Always runs
                .anyMatch(msg -> msg.contains("Before stopping automation")) // Runs before stopAutomation
                .noneMatch(msg -> msg.contains("This should never be logged")) // Must not run after stopAutomation
                .noneMatch(msg -> msg.contains("Before stopping sequence")) // Must not execute second IF (automation already stopped)
                .noneMatch(msg -> msg.contains("Final log after sequence stop")); // Final action should not run (automation is stopped)

        // Now, let's test stopActionSequence without triggering stopAutomation
        var yamlWithoutStopAutomation = """
                alias: Stop Action Sequence Test
                triggers:
                  - trigger: time
                    at: 13:00
                actions:
                  - action: logger
                    message: "Start sequence"
                
                  # First IF block does NOT stop automation
                  - action: ifThenElse
                    if:
                      - condition: time
                        before: 14:00
                    then:
                      - action: logger
                        message: "Before stopping sequence"
                      - action: stop
                        condition:
                          condition: alwaysTrue
                        stopActionSequence: true
                      - action: logger
                        message: "This should not be logged inside IF block"
                
                  # Actions outside IF should still execute after stopActionSequence
                  - action: logger
                    message: "Final log after sequence stop"
                """;

        Automation automation2 = factory.createAutomation("yaml", yamlWithoutStopAutomation);
        engine.register(automation2);

        // Act: Create event for second test
        TimeBasedEvent eventAt1PM = new TimeBasedEvent(LocalTime.of(13, 0));

        // Process event
        engine.publishEvent(eventAt1PM);

        // Assert:
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Start sequence"))  // Always runs
                .anyMatch(msg -> msg.contains("Before stopping sequence")) // Runs before stopActionSequence
                .noneMatch(msg -> msg.contains("This should not be logged inside IF block")) // Must not log due to sequence stop
                .anyMatch(msg -> msg.contains("Final log after sequence stop")); // Final action should execute (automation still running)
    }

}