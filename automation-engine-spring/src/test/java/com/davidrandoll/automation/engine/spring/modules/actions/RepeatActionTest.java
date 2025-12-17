package com.davidrandoll.automation.engine.spring.modules.actions;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
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

    @Test
    void testRepeatActionWithForEachIntegerList() {
        var yaml = """
                alias: Repeat For Each Integer List Test
                triggers:
                  - trigger: time
                    at: 10:00
                actions:
                  - action: repeat
                    foreach: [1, 2, 3, 4, 5]
                    actions:
                      - action: logger
                        message: "Processing number {{ item }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(10, 0)));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Processing number 1"))
                .anyMatch(msg -> msg.contains("Processing number 2"))
                .anyMatch(msg -> msg.contains("Processing number 3"))
                .anyMatch(msg -> msg.contains("Processing number 4"))
                .anyMatch(msg -> msg.contains("Processing number 5"));
    }

    @Test
    void testRepeatActionWithForEachDoubleList() {
        var yaml = """
                alias: Repeat For Each Double List Test
                triggers:
                  - trigger: time
                    at: 10:15
                actions:
                  - action: repeat
                    foreach: [1.5, 2.7, 3.14]
                    actions:
                      - action: logger
                        message: "Processing decimal {{ item }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(10, 15)));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Processing decimal 1.5"))
                .anyMatch(msg -> msg.contains("Processing decimal 2.7"))
                .anyMatch(msg -> msg.contains("Processing decimal 3.14"));
    }

    @Test
    void testRepeatActionWithCustomVariableName() {
        var yaml = """
                alias: Repeat With Custom Variable Name Test
                triggers:
                  - trigger: time
                    at: 10:30
                actions:
                  - action: repeat
                    foreach: ["ID001", "ID002", "ID003"]
                    as: "ownerId"
                    actions:
                      - action: logger
                        message: "Processing owner ID: {{ ownerId }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(10, 30)));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Processing owner ID: ID001"))
                .anyMatch(msg -> msg.contains("Processing owner ID: ID002"))
                .anyMatch(msg -> msg.contains("Processing owner ID: ID003"));
    }

    @Test
    void testRepeatActionWithTemplateExpression() {
        var yaml = """
                alias: Repeat With Template Expression Test
                triggers:
                  - trigger: time
                    at: 10:45
                actions:
                  - action: variable
                    userIds: ["user1", "user2", "user3"]
                  - action: repeat
                    foreach: "{{ userIds }}"
                    as: "userId"
                    actions:
                      - action: logger
                        message: "Processing user: {{ userId }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(10, 45)));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Processing user: user1"))
                .anyMatch(msg -> msg.contains("Processing user: user2"))
                .anyMatch(msg -> msg.contains("Processing user: user3"));
    }

    @Test
    void testRepeatActionWithMixedTypes() {
        var yaml = """
                alias: Repeat With Mixed Types Test
                triggers:
                  - trigger: time
                    at: 11:00
                actions:
                  - action: repeat
                    foreach: ["string", 123, 45.67, true]
                    as: "value"
                    actions:
                      - action: logger
                        message: "Value: {{ value }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(11, 0)));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Value: string"))
                .anyMatch(msg -> msg.contains("Value: 123"))
                .anyMatch(msg -> msg.contains("Value: 45.67"))
                .anyMatch(msg -> msg.contains("Value: true"));
    }

    @Test
    void testRepeatActionWithMapObjects() {
        var yaml = """
                alias: Repeat With Map Objects Test
                triggers:
                  - trigger: time
                    at: 11:15
                actions:
                  - action: repeat
                    foreach:
                      - id: 1
                        name: "Alice"
                        age: 30
                      - id: 2
                        name: "Bob"
                        age: 25
                      - id: 3
                        name: "Charlie"
                        age: 35
                    as: "person"
                    actions:
                      - action: logger
                        message: "Person {{ person.id }}: {{ person.name }}, Age {{ person.age }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(11, 15)));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Person 1: Alice, Age 30"))
                .anyMatch(msg -> msg.contains("Person 2: Bob, Age 25"))
                .anyMatch(msg -> msg.contains("Person 3: Charlie, Age 35"));
    }

    @Test
    void testRepeatActionWithNestedObjects() {
        var yaml = """
                alias: Repeat With Nested Objects Test
                triggers:
                  - trigger: time
                    at: 11:30
                actions:
                  - action: repeat
                    foreach:
                      - user:
                          id: "u1"
                          email: "alice@example.com"
                        role: "admin"
                      - user:
                          id: "u2"
                          email: "bob@example.com"
                        role: "user"
                    as: "record"
                    actions:
                      - action: logger
                        message: "User {{ record.user.id }} ({{ record.user.email }}) has role: {{ record.role }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(11, 30)));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("User u1 (alice@example.com) has role: admin"))
                .anyMatch(msg -> msg.contains("User u2 (bob@example.com) has role: user"));
    }

    @Test
    void testRepeatActionWithEmptyList() {
        var yaml = """
                alias: Repeat With Empty List Test
                triggers:
                  - trigger: time
                    at: 11:45
                actions:
                  - action: repeat
                    foreach: []
                    actions:
                      - action: logger
                        message: "This should not appear"
                  - action: logger
                    message: "After repeat"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(11, 45)));

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not appear"))
                .anyMatch(msg -> msg.contains("After repeat"));
    }

    @Test
    void testRepeatActionWithSingleValue() {
        var yaml = """
                alias: Repeat With Single Value Test
                triggers:
                  - trigger: time
                    at: 12:00
                actions:
                  - action: variable
                    singleValue: "single-item"
                  - action: repeat
                    foreach: "{{ singleValue }}"
                    as: "value"
                    actions:
                      - action: logger
                        message: "Processing: {{ value }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(12, 0)));

        assertThat(logAppender.getLoggedMessages())
                .filteredOn(msg -> msg.contains("Processing: single-item"))
                .hasSize(1);
    }

    @Test
    void testRepeatActionWithBooleanList() {
        var yaml = """
                alias: Repeat With Boolean List Test
                triggers:
                  - trigger: time
                    at: 12:15
                actions:
                  - action: repeat
                    foreach: [true, false, true]
                    as: "flag"
                    actions:
                      - action: logger
                        message: "Flag value: {{ flag }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        engine.publishEvent(new TimeBasedEvent(LocalTime.of(12, 15)));

        assertThat(logAppender.getLoggedMessages())
                .filteredOn(msg -> msg.contains("Flag value:"))
                .hasSize(3);
    }
}
