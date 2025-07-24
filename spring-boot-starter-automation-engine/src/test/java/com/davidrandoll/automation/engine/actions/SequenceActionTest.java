package com.davidrandoll.automation.engine.actions;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;


class SequenceActionTest extends AutomationEngineTest {

    @Test
    void testSequenceActionsExecuteInOrder() {
        var yaml = """
                alias: Sequence Actions Test
                triggers:
                  - trigger: time
                    at: 10:00
                actions:
                  - action: logger
                    message: "Before sequence"
                  - action: sequence
                    actions:
                      - action: logger
                        message: "Action 1 in sequence"
                      - action: logger
                        message: "Action 2 in sequence"
                  - action: logger
                    message: "After sequence"
                """;

        // Create automation from YAML
        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt10AM = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(eventAt10AM);

        // Assert: Ensure all actions were logged in the correct order
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Before sequence"))
                .anyMatch(msg -> msg.contains("Action 1 in sequence"))
                .anyMatch(msg -> msg.contains("Action 2 in sequence"))
                .anyMatch(msg -> msg.contains("After sequence"));
    }

}