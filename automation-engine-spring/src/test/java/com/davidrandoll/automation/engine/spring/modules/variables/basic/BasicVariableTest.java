package com.davidrandoll.automation.engine.spring.modules.variables.basic;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class BasicVariableTest extends AutomationEngineTest {

    @Test
    void testVariableActionSetsVariableAndLoggerUsesIt() {
        var yaml = """
                alias: Test Variable in Logger
                triggers:
                  - trigger: time
                    at: 14:00
                variables:
                  - someVar: "14:00"
                actions:
                  - action: logger
                    message: "Automation triggered at {{ someVar }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent eventAt14 = new TimeBasedEvent(LocalTime.of(14, 0));
        engine.publishEvent(eventAt14);

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
                variables:
                  - someVar: "15:30"
                actions:
                  - action: logger
                    message: "Automation triggered at {{ someVar | time_format(pattern='hh:mm a') }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent eventAt1530 = new TimeBasedEvent(LocalTime.of(15, 30));
        engine.publishEvent(eventAt1530);

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
                variables:
                  - log_message: "Dynamic log message!"
                actions:
                  - action: logger
                    message: "{{ log_message }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent eventAt1645 = new TimeBasedEvent(LocalTime.of(16, 45));
        engine.publishEvent(eventAt1645);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Dynamic log message!"));
    }
}