package com.davidrandoll.automation.engine.actions;

import com.davidrandoll.automation.engine.TestConfig;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestConfig.class)
class WaitForTriggerActionTest extends AutomationEngineTest {

    @Test
    void testWaitForTriggerActionWithNoTriggers() {
        var yaml = """
                alias: Wait for Trigger with No Triggers
                triggers:
                  - trigger: time
                    at: 12:00
                actions:
                  - action: waitForTrigger
                    timeout: PT5S
                    triggers: []
                  - action: logger
                    message: "This should be logged immediately"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAtNoon = new TimeBasedEvent(LocalTime.of(12, 0));
        engine.publishEvent(eventAtNoon);

        // Assert: The logger message should be logged immediately since there are no triggers to wait for
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("This should be logged immediately"));
    }

    @Test
    void testWaitForTriggerActionWithTriggerActivatedEarly() {
        var yaml = """
                alias: Wait for Trigger with Early Activation
                triggers:
                  - trigger: time
                    at: 14:00
                actions:
                  - action: logger
                    message: "Waiting for trigger..."
                  - action: waitForTrigger
                    timeout: PT10S
                    triggers:
                      - trigger: time
                        at: 14:00:05
                  - action: logger
                    message: "Trigger activated before timeout!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that satisfies the trigger early
        TimeBasedEvent eventAt1405 = new TimeBasedEvent(LocalTime.of(14, 0));

        // Process event (should trigger action before timeout)
        engine.publishEvent(eventAt1405);

        // Assert: The waiting log should appear, and the next action should execute immediately after trigger is fired
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Waiting for trigger..."))
                .anyMatch(msg -> msg.contains("Trigger activated before timeout!"));
    }

    @Test
    void testWaitForTriggerActionTimesOutWhenNoTriggerOccurs() {
        var yaml = """
                alias: Wait for Trigger Timeout
                triggers:
                  - trigger: time
                    at: 16:00
                actions:
                  - action: logger
                    message: "Waiting for trigger..."
                  - action: waitForTrigger
                    timeout: PT3S
                    triggers:
                      - trigger: time
                        at: 16:01
                  - action: logger
                    message: "This should be logged after timeout"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger automation, but do NOT send the expected trigger event
        long startTime = System.currentTimeMillis();
        TimeBasedEvent eventAt4PM = new TimeBasedEvent(LocalTime.of(16, 0));
        engine.publishEvent(eventAt4PM);

        // Assert: The waiting log should appear, and the action should proceed after timeout
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Waiting for trigger..."))
                .anyMatch(msg -> msg.contains("This should be logged after timeout"));

        // Measure elapsed time
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertThat(elapsedTime).isLessThan(3500); // 3 seconds
    }

    @Test
    void testWaitForTriggerActionUsesDefaultTimeout() {
        var yaml = """
                alias: Wait for Trigger with Default Timeout
                triggers:
                  - trigger: time
                    at: 18:00
                actions:
                  - action: logger
                    message: "Waiting with default timeout..."
                  - action: waitForTrigger
                    triggers:
                      - trigger: time
                        at: 18:10
                  - action: logger
                    message: "Action proceeds after default timeout"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger automation without sending the expected trigger event
        long startTime = System.currentTimeMillis();
        TimeBasedEvent eventAt6PM = new TimeBasedEvent(LocalTime.of(18, 0));
        engine.publishEvent(eventAt6PM);

        // Assert: The waiting message should appear, and the next action should proceed after the default timeout of 60 seconds
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Waiting with default timeout..."))
                .anyMatch(msg -> msg.contains("Action proceeds after default timeout"));

        // Measure elapsed time
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertThat(elapsedTime).isLessThan(2000); // 60 seconds
    }

    @Test
    void testWaitForTriggerActionWithLongTimeoutButEarlyTrigger() {
        LocalTime time = LocalTime.now().withNano(0);
        // add x amount of seconds to the current time so that the next minute is always more than 30 seconds away
        LocalTime waitForTriggerTime = null;
        if (time.getSecond() > 30) {
            // this means that time is greater than 30 seconds
            // so for example if time is 14:00:45, we want the next minute away to be more than 30 seconds away
            // so we need to add 90 seconds to the current time to guarantee that is more than 30 seconds away
            waitForTriggerTime = time.plusSeconds(90);
        } else {
            // this means that time is less than 30 seconds
            // so for example if time is 14:00:15, we want the next minute away to be more than 30 seconds away
            // so we need to add 60 seconds to the current time to guarantee that is more than 30 seconds away
            waitForTriggerTime = time.plusSeconds(60);
        }
        var yaml = """
                alias: Wait for Trigger with Long Timeout But Early Trigger
                triggers:
                  - trigger: time
                    at: %s
                actions:
                  - action: logger
                    message: "Waiting for trigger..."
                  - action: waitForTrigger
                    timeout: PT2M
                    triggers:
                      - trigger: time
                        at: %s
                  - action: logger
                    message: "Trigger met, proceeding early!"
                """.formatted(time, waitForTriggerTime);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger automation at 19:00 and record the start time
        long startTime = System.currentTimeMillis();
        TimeBasedEvent eventAt1900 = new TimeBasedEvent(time);
        engine.publishEvent(eventAt1900);

        // Measure elapsed time
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Assert: The waiting log should appear, and execution should proceed after 1 minute, not 5
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Waiting for trigger..."))
                .anyMatch(msg -> msg.contains("Trigger met, proceeding early!"));

        assertThat(elapsedTime).isGreaterThan(20_000).isLessThan(90_000);
    }

    @Test
    void testWaitForTriggerActionTriggerBeforeTimeout() {
        var yaml = """
                alias: Wait for Trigger with Default Timeout
                triggers:
                  - trigger: time
                    at: 18:00
                actions:
                  - action: logger
                    message: "Waiting with default timeout..."
                  - action: waitForTrigger
                    timeout: PT10S
                    triggers:
                      - trigger: alwaysTrue
                  - action: logger
                    message: "Action proceeds after default timeout"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger automation without sending the expected trigger event
        long startTime = System.currentTimeMillis();
        TimeBasedEvent eventAt6PM = new TimeBasedEvent(LocalTime.of(18, 0));
        engine.publishEvent(eventAt6PM);

        // Measure elapsed time
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Assert: The waiting message should appear, and the next action should proceed after the default timeout of 60 seconds
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Waiting with default timeout..."))
                .anyMatch(msg -> msg.contains("Action proceeds after default timeout"));

        assertThat(elapsedTime).isLessThan(1_000);

    }

    @Test
    void testWaitForTriggerActionTriggerTimeout() {
        var yaml = """
                alias: Wait for Trigger with Default Timeout
                triggers:
                  - trigger: time
                    at: 18:00
                actions:
                  - action: logger
                    message: "Waiting with default timeout..."
                  - action: waitForTrigger
                    timeout: PT3S
                    triggers:
                      - trigger: alwaysFalse
                  - action: logger
                    message: "Action proceeds after default timeout"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger automation without sending the expected trigger event
        long startTime = System.currentTimeMillis();
        TimeBasedEvent eventAt6PM = new TimeBasedEvent(LocalTime.of(18, 0));
        engine.publishEvent(eventAt6PM);

        // Measure elapsed time
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Assert: The waiting message should appear, and the next action should proceed after the default timeout of 60 seconds
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Waiting with default timeout..."))
                .anyMatch(msg -> msg.contains("Action proceeds after default timeout"));

        assertThat(elapsedTime).isGreaterThan(3_000);

    }
}