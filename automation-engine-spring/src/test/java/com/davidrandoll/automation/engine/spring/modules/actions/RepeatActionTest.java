package com.davidrandoll.automation.engine.spring.modules.actions;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class RepeatActionTest extends AutomationEngineTest {

    @Test
    void testRepeatActionWithCount() {
        var yaml = """
                alias: Repeat Action Count Test
                triggers:
                  - trigger: time
                    at: 12:00
                actions:
                  - action: repeat
                    count: 3
                    actions:
                      - action: logger
                        message: "Repeated action executed"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Trigger the automation
        engine.publishEvent(new TimeBasedEvent(LocalTime.of(12, 0)));

        // Assert that the message appears exactly 3 times
        assertThat(logAppender.getLoggedMessages())
                .filteredOn(msg -> msg.contains("Repeated action executed"))
                .hasSize(3);
    }

    @Test
    void testRepeatActionWithWhileCondition() {
        var yaml = """
                alias: Repeat While Condition Met
                triggers:
                  - trigger: time
                    at: 13:00
                actions:
                  - action: variable
                    counter: 0
                  - action: repeat
                    while:
                      - condition: template
                        expression: "{{ (counter | int) < 3 }}"
                    actions:
                      - action: logger
                        message: "Repeating while counter < 3"
                      - action: variable
                        counter: "{{ (counter | int) + 1 }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Trigger the automation
        engine.publishEvent(new TimeBasedEvent(LocalTime.of(13, 0)));

        // Assert that the logger message appears exactly 3 times
        assertThat(logAppender.getLoggedMessages())
                .filteredOn(msg -> msg.contains("Repeating while counter < 3"))
                .hasSize(3);
    }

    @Test
    void testRepeatActionWithUntilCondition() {
        var yaml = """
                alias: Repeat Until Condition Met
                triggers:
                  - trigger: time
                    at: 14:00
                actions:
                  - action: variable
                    counter: 0
                  - action: repeat
                    until:
                      - condition: template
                        expression: "{{ (counter | int) >= 3 }}"
                    actions:
                      - action: logger
                        message: "Repeating until counter >= 3"
                      - action: variable
                        counter: "{{ (counter | int) + 1 }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Trigger the automation
        engine.publishEvent(new TimeBasedEvent(LocalTime.of(14, 0)));

        // Assert that the logger message appears exactly 3 times
        assertThat(logAppender.getLoggedMessages())
                .filteredOn(msg -> msg.contains("Repeating until counter >= 3"))
                .hasSize(3);
    }

    @Test
    void testRepeatActionWithForEachList() {
        var yaml = """
                alias: Repeat For Each List Test
                triggers:
                  - trigger: time
                    at: 15:00
                actions:
                  - action: repeat
                    for_each: ["apple", "banana", "cherry"]
                    actions:
                      - action: logger
                        message: "Processing {{ item }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Trigger the automation
        engine.publishEvent(new TimeBasedEvent(LocalTime.of(15, 0)));

        // Assert that the messages appear with correct values
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Processing apple"))
                .anyMatch(msg -> msg.contains("Processing banana"))
                .anyMatch(msg -> msg.contains("Processing cherry"));
    }

    @Test
    void testRepeatActionWithForEachMap() {
        var yaml = """
                alias: Repeat For Each Map Test
                triggers:
                  - trigger: time
                    at: 16:00
                actions:
                  - action: repeat
                    for_each:
                      - someVar1: "value1"
                        someVar2: "value2"
                      - someVar1: "value3"
                        someVar2: "value4"
                    actions:
                      - action: logger
                        message: "Key: {{ item.someVar1 }}, Value: {{ item.someVar2 }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Trigger the automation
        engine.publishEvent(new TimeBasedEvent(LocalTime.of(16, 0)));

        // Assert that the messages appear with correct key-value pairs
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Key: value1, Value: value2"))
                .anyMatch(msg -> msg.contains("Key: value3, Value: value4"));
    }
}
