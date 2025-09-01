package com.davidrandoll.automation.engine.spring.modules.triggers.on_event_type;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.events.JsonEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static com.davidrandoll.automation.engine.test.JsonTestUtils.json;
import static org.assertj.core.api.Assertions.assertThat;

class OnEventTypeTriggerTest extends AutomationEngineTest {

    @Test
    void testTriggersWhenEventTypeMatchesExactly() {
        var yaml = """
                triggers:
                  - trigger: onEventType
                    eventType: com.example.MyEvent
                actions:
                  - action: logger
                    message: "Matched full event type"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        JsonNode event = json("""
                {
                  "data": "123",
                  "eventType": "com.example.MyEvent"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Matched full event type"));
    }

    @Test
    void testTriggersWhenEventNameMatches() {
        var yaml = """
                triggers:
                  - trigger: onEventType
                    eventName: MyEvent
                actions:
                  - action: logger
                    message: "Matched simple event name"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = json("""
                {
                  "someField": "abc",
                  "eventType": "com.example.MyEvent"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Matched simple event name"));
    }

    @Test
    void testTriggersWhenRegexMatchesEventType() {
        var yaml = """
                triggers:
                  - trigger: onEventType
                    regex: .*MyEvent
                actions:
                  - action: logger
                    message: "Matched via regex"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = json("""
                {
                  "payload": {},
                  "eventType": "com.example.MyEvent"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Matched via regex"));
    }

    @Test
    void testDoesNotTriggerWhenEventTypeDoesNotMatch() {
        var yaml = """
                triggers:
                  - trigger: onEventType
                    eventType: com.example.UnmatchedEvent
                actions:
                  - action: logger
                    message: "Should not trigger"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = json("""
                {
                  "info": "test",
                  "eventType": "com.example.SomethingElse"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testDoesNotTriggerWhenNoEventTypePresent() {
        var yaml = """
                triggers:
                  - trigger: onEventType
                    eventName: MyEvent
                actions:
                  - action: logger
                    message: "Should not trigger without eventType"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = json("""
                {
                  "message": "missing eventType"
                }
                """);
        engine.publishEvent(new JsonEvent(event));

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger without eventType"));
    }
}
