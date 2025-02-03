package com.automation.engine.action_building_block;

import ch.qos.logback.classic.Logger;
import com.automation.engine.AutomationEngineApplication;
import com.automation.engine.TestLogAppender;
import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
import com.automation.engine.modules.time_based.event.TimeBasedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
@ExtendWith(SpringExtension.class)
public class WaitForTriggerActionTest {
    @Autowired
    private AutomationEngine engine;

    @Autowired
    private AutomationFactory factory;

    private TestLogAppender logAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger("com.automation.engine");
        logAppender = new TestLogAppender();
        logger.addAppender(logAppender);
        logAppender.start();

        engine.clearAutomations();
    }

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
        engine.addAutomation(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAtNoon = new TimeBasedEvent(LocalTime.of(12, 0));
        engine.processEvent(eventAtNoon);

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
        engine.addAutomation(automation);

        // Act: Create event that satisfies the trigger early
        TimeBasedEvent eventAt1405 = new TimeBasedEvent(LocalTime.of(14, 0));

        // Process event (should trigger action before timeout)
        engine.processEvent(eventAt1405);

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
        engine.addAutomation(automation);

        // Act: Trigger automation, but do NOT send the expected trigger event
        TimeBasedEvent eventAt4PM = new TimeBasedEvent(LocalTime.of(16, 0));
        engine.processEvent(eventAt4PM);

        // Assert: The waiting log should appear, and the action should proceed after timeout
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Waiting for trigger..."))
                .anyMatch(msg -> msg.contains("This should be logged after timeout"));
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
        engine.addAutomation(automation);

        // Act: Trigger automation without sending the expected trigger event
        TimeBasedEvent eventAt6PM = new TimeBasedEvent(LocalTime.of(18, 0));
        engine.processEvent(eventAt6PM);

        // Assert: The waiting message should appear, and the next action should proceed after the default timeout of 60 seconds
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Waiting with default timeout..."))
                .anyMatch(msg -> msg.contains("Action proceeds after default timeout"));
    }

    @Test
    void testWaitForTriggerActionWithLongTimeoutButEarlyTrigger() {
        var yaml = """
                alias: Wait for Trigger with Long Timeout But Early Trigger
                triggers:
                  - trigger: time
                    at: 19:00
                actions:
                  - action: logger
                    message: "Waiting for trigger..."
                  - action: waitForTrigger
                    timeout: PT5M
                    triggers:
                      - trigger: time
                        at: 19:01
                  - action: logger
                    message: "Trigger met, proceeding early!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Trigger automation at 19:00
        TimeBasedEvent eventAt1900 = new TimeBasedEvent(LocalTime.of(19, 0));
        engine.processEvent(eventAt1900);

        // Assert: The waiting log should appear, and execution should proceed after 1 minute, not 5
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Waiting for trigger..."))
                .anyMatch(msg -> msg.contains("Trigger met, proceeding early!"));
    }

}