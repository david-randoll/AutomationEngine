package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_latency;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.spring.web.modules.triggers.on_slow_http_request.OnSlowHttpRequestException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class HttpLatencyConditionTest extends AutomationEngineTest {


    @Test
    void testLatencyExceedsThresholdShouldPass() {
        var yaml = """
                alias: latency-high
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                    seconds: 2
                actions:
                  - action: logger
                    message: latency was high
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("duration", Duration.ofSeconds(3)); // Simulate 3 seconds
        var event = AEHttpResponseEvent.builder()
                .additionalData(additionalData)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testLatencyBelowThresholdShouldFail() {
        var yaml = """
                alias: latency-low
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                    milliseconds: 1500
                actions:
                  - action: logger
                    message: latency too low
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("duration", Duration.ofMillis(500));
        var event = AEHttpResponseEvent.builder()
                .additionalData(additionalData)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testLatencyUsingMinutesShouldPass() {
        var yaml = """
                alias: latency-minutes
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                    minutes: 1
                actions:
                  - action: logger
                    message: latency over 1 minute
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("duration", Duration.ofMinutes(2)); // Simulate 2 minutes
        var event = AEHttpResponseEvent.builder()
                .additionalData(additionalData)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testMissingEventDurationShouldThrow() {
        var yaml = """
                alias: latency-missing-event-duration
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                    seconds: 1
                actions:
                  - action: logger
                    message: should not log
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder().build(); // No duration
        var context = EventContext.of(event);

        assertThatThrownBy(() -> automation.allConditionsMet(context))
                .isInstanceOf(OnSlowHttpRequestException.class)
                .hasMessageContaining("Duration not found in event");
    }

    @Test
    void testMissingContextDurationShouldThrow() {
        var yaml = """
                alias: latency-missing-context-duration
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                actions:
                  - action: logger
                    message: should not log
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("duration", Duration.ofSeconds(2));
        var event = AEHttpResponseEvent.builder()
                .additionalData(additionalData)
                .build();

        var context = EventContext.of(event);

        assertThatThrownBy(() -> automation.allConditionsMet(context))
                .isInstanceOf(OnSlowHttpRequestException.class)
                .hasMessageContaining("Duration not found in trigger context");
    }

    @Test
    void testExactMatchShouldNotPass() {
        var yaml = """
                alias: latency-exact-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                    milliseconds: 1000
                actions:
                  - action: logger
                    message: latency is exactly 1s
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("duration", Duration.ofMillis(1000)); // Simulate exact match
        var event = AEHttpResponseEvent.builder()
                .additionalData(additionalData)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        // latency must be GREATER THAN, not equal
        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testNegativeDurationStillWorksIfGreaterThanContext() {
        var yaml = """
                alias: latency-negative-event
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                    milliseconds: -2000
                actions:
                  - action: logger
                    message: event latency still greater
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("duration", Duration.ofMillis(-1000)); // Simulate negative duration
        var event = AEHttpResponseEvent.builder()
                .additionalData(additionalData)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue(); // -1000 > -2000
    }

    @Test
    void testNegativeDurationInContextFailsIfNotGreater() {
        var yaml = """
                alias: latency-negative-context
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                    milliseconds: -500
                actions:
                  - action: logger
                    message: only log if duration > -500ms
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("duration", Duration.ofMillis(-600)); // Simulate negative duration
        var event = AEHttpResponseEvent.builder()
                .additionalData(additionalData)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse(); // -600 < -500
    }

    @Test
    void testNullEventDurationThrows() {
        var yaml = """
                alias: latency-null-event
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                    seconds: 2
                actions:
                  - action: logger
                    message: should throw
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("duration", null); // Simulate null duration
        var event = AEHttpResponseEvent.builder()
                .additionalData(additionalData)
                .build();

        var context = EventContext.of(event);

        assertThatThrownBy(() -> automation.allConditionsMet(context))
                .isInstanceOf(OnSlowHttpRequestException.class)
                .hasMessageContaining("Duration not found in event");
    }

    @Test
    void testNullContextDurationThrows() {
        var yaml = """
                alias: latency-null-context
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpLatency
                actions:
                  - action: logger
                    message: should throw
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> additionalData = Map.of("duration", Duration.ofSeconds(1));
        var event = AEHttpResponseEvent.builder()
                .additionalData(additionalData)
                .build();

        var context = EventContext.of(event);

        assertThatThrownBy(() -> automation.allConditionsMet(context))
                .isInstanceOf(OnSlowHttpRequestException.class)
                .hasMessageContaining("Duration not found in trigger context");
    }

}