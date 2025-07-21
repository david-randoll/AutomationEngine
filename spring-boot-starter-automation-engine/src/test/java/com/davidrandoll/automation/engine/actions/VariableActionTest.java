package com.davidrandoll.automation.engine.actions;

import com.davidrandoll.automation.engine.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class VariableActionTest extends AutomationEngineTest {

    @Test
    void testVariableActionSetsVariableAndLoggerUsesIt() {
        var yaml = """
                alias: Test Variable in Logger
                triggers:
                  - trigger: time
                    at: 14:00
                actions:
                  - action: variable
                    someVar: "14:00"
                    time: 17:00
                  - action: logger
                    message: "Automation triggered at {{ someVar }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger automation at 14:00
        TimeBasedEvent eventAt14 = new TimeBasedEvent(LocalTime.of(14, 0));
        engine.publishEvent(eventAt14);

        // Assert: Ensure the log contains the resolved message
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered at 14:00"));
    }

    @Test
    void testVariableActionWithTimeFormatting() {
        var yaml = """
                alias: Test Variable with Time Formatting
                triggers:
                  - trigger: time
                    at: 15:30
                actions:
                  - action: variable
                    someVar: "15:30"
                  - action: logger
                    message: "Automation triggered at {{ someVar | time_format(pattern='hh:mm a') }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger automation at 15:30
        TimeBasedEvent eventAt1530 = new TimeBasedEvent(LocalTime.of(15, 30));
        engine.publishEvent(eventAt1530);

        // Assert: Ensure the log contains the formatted time
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered at 03:30 PM"));
    }

    @Test
    void testLoggerUsesMessageSetInVariable() {
        var yaml = """
                alias: Test Logger Uses Variable Message
                triggers:
                  - trigger: time
                    at: 16:45
                actions:
                  - action: variable
                    log_message: "Dynamic log message!"
                  - action: logger
                    message: "{{ log_message }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger automation at 16:45
        TimeBasedEvent eventAt1645 = new TimeBasedEvent(LocalTime.of(16, 45));
        engine.publishEvent(eventAt1645);

        // Assert: Ensure the logger correctly logs the message stored in the variable
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Dynamic log message!"));
    }

    @Test
    void testConditionalVariableAssignmentAndLogging() {
        var yaml = """
                alias: Test Conditional Variable Assignment
                triggers:
                  - trigger: time
                    at: 18:00
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        before: 18:30
                    then:
                      - action: variable
                        status_message: "Its still early!"
                    else:
                      - action: variable
                        status_message: "Its getting late!"
                  - action: logger
                    message: "{{ status_message }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger automation at 18:00 (before 18:30, so 'then' branch should execute)
        TimeBasedEvent eventAt18 = new TimeBasedEvent(LocalTime.of(18, 0));
        engine.publishEvent(eventAt18);

        // Assert: Ensure the logger outputs the correct message based on the condition
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Its still early!"))
                .noneMatch(msg -> msg.contains("Its getting late!"));
    }


}
