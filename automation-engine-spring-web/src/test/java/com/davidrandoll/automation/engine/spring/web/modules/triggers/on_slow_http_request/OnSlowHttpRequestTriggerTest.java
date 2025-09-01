package com.davidrandoll.automation.engine.spring.web.modules.triggers.on_slow_http_request;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class OnSlowHttpRequestTriggerTest extends AutomationEngineTest {


    @Test
    void testShouldTriggerWhenEventDurationIsGreater() {
        var yaml = """
                alias: slow request trigger
                triggers:
                  - trigger: onSlowHttpRequest
                    duration: PT2S
                actions:
                  - action: logger
                    message: request was slow
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK)
                .build();

        event.addAdditionalData("duration", Duration.ofSeconds(3));

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerWhenEventDurationIsLess() {
        var yaml = """
                alias: not slow enough
                triggers:
                  - trigger: onSlowHttpRequest
                    duration: PT3S
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK)
                .build();

        event.addAdditionalData("duration", Duration.ofSeconds(2));

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldTriggerWithNanos() {
        var yaml = """
                alias: nanos edge
                triggers:
                  - trigger: onSlowHttpRequest
                    duration: PT1.000000001S
                actions:
                  - action: logger
                    message: nanoseconds test
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder().build();
        event.addAdditionalData("duration", Duration.ofNanos(2_000_000_000)); // 2s

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldThrowIfEventMissingDuration() {
        var yaml = """
                alias: missing event duration
                triggers:
                  - trigger: onSlowHttpRequest
                    duration: PT1S
                actions:
                  - action: logger
                    message: should fail
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder().build(); // no duration

        var context = EventContext.of(event);

        assertThatThrownBy(() -> engine.publishEvent(context))
                .isInstanceOf(OnSlowHttpRequestException.class)
                .hasMessageContaining("Duration not found in event");
    }

    @Test
    void testShouldThrowIfTriggerMissingDuration() {
        var trigger = new OnSlowHttpRequestTrigger();

        var context = new OnSlowHttpRequestContext(); // no duration set
        var event = AEHttpResponseEvent.builder().build();
        event.addAdditionalData("duration", Duration.ofSeconds(5));

        var ec = EventContext.of(event);

        assertThatThrownBy(() -> trigger.isTriggered(ec, context))
                .isInstanceOf(OnSlowHttpRequestException.class)
                .hasMessageContaining("Duration not found in trigger context");
    }

    @Test
    void testShouldNotTriggerOnNonAEHttpResponseEvent() {
        var trigger = new OnSlowHttpRequestTrigger();
        var context = new OnSlowHttpRequestContext();
        context.setDuration(Duration.ofSeconds(1));
        var ec = EventContext.of(new TimeBasedEvent(LocalTime.now()));

        assertThat(trigger.isTriggered(ec, context)).isFalse();
    }
}