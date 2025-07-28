package com.davidrandoll.automation.engine.spring.events.modules.publish_spring_event;

import com.davidrandoll.automation.engine.modules.events.DefaultEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.test.EventCaptureListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublishSpringEventActionTest extends AutomationEngineTest {
    @Autowired
    private EventCaptureListener eventCaptureListener;

    @BeforeEach
    void clear() {
        eventCaptureListener.clearEvents();
    }

    @Test
    void testPublishesCustomEventByClassName() {
        String yaml = """
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: publishSpringEvent
                    className: %s
                    data:
                      message: "hello!"
                """.formatted(TestCustomEvent.class.getName());

        engine.register(factory.createAutomation("yaml", yaml));
        engine.publishEvent(new DefaultEvent());

        var event = eventCaptureListener.getEvents().stream()
                .filter(e -> e instanceof TestCustomEvent)
                .map(e -> (TestCustomEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No TestCustomEvent found"));

        assertThat(event.getMessage()).isEqualTo("hello!");
    }

    @Test
    void testPublishesGenericPublishSpringEventWhenNoClassNameProvided() {
        String yaml = """
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: publishSpringEvent
                    data:
                      source: "automation"
                      payload: "just a map"
                """;

        engine.register(factory.createAutomation("yaml", yaml));
        engine.publishEvent(new DefaultEvent());

        var event = eventCaptureListener.getEvents().stream()
                .filter(e -> e instanceof PublishSpringEvent)
                .map(e -> (PublishSpringEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No PublishSpringEvent found"));

        assertThat(event.getData()).containsEntry("source", "automation");
        assertThat(event.getData()).containsEntry("payload", "just a map");
    }

    @Test
    void testHandlesInvalidClassGracefully() {
        String yaml = """
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: publishSpringEvent
                    className: com.example.DoesNotExist
                    data:
                      x: "y"
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        assertThatThrownBy(() -> engine.publishEvent(new DefaultEvent()))
                .isInstanceOf(AEClassNotFoundException.class)
                .hasMessageContaining("com.example.DoesNotExist");
    }
}