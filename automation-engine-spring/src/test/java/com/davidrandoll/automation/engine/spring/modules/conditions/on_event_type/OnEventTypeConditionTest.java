package com.davidrandoll.automation.engine.spring.modules.conditions.on_event_type;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.creator.events.JsonEvent;
import org.junit.jupiter.api.Test;

import static com.davidrandoll.automation.engine.test.JsonTestUtils.json;
import static org.assertj.core.api.Assertions.assertThat;

class OnEventTypeConditionTest extends AutomationEngineTest {

    @Test
    void testConditionSatisfiedByExactEventType() {
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onEventType
                    eventType: com.example.MyEvent
                actions:
                  - action: logger
                    message: "Condition matched exact eventType"
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        var event = json("""
                {
                  "data": "test",
                  "eventType": "com.example.MyEvent"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Condition matched exact eventType"));
    }

    @Test
    void testConditionSatisfiedByEventName() {
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onEventType
                    eventName: MyEvent
                actions:
                  - action: logger
                    message: "Condition matched eventName"
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        var event = json("""
                {
                  "eventType": "com.example.MyEvent"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Condition matched eventName"));
    }

    @Test
    void testConditionSatisfiedByRegex() {
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onEventType
                    regex: .*MyEvent
                actions:
                  - action: logger
                    message: "Condition matched regex"
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        var event = json("""
                {
                  "eventType": "com.example.MyEvent"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Condition matched regex"));
    }

    @Test
    void testConditionNotSatisfiedIfNoMatch() {
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onEventType
                    eventType: com.example.OtherEvent
                actions:
                  - action: logger
                    message: "Should not trigger"
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        var event = json("""
                {
                  "eventType": "com.example.SomeEvent"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testConditionNotSatisfiedWithoutEventType() {
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onEventType
                    eventName: MyEvent
                actions:
                  - action: logger
                    message: "Should not trigger without eventType"
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        var event = json("""
                {
                  "someField": "value"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger without eventType"));
    }
}
