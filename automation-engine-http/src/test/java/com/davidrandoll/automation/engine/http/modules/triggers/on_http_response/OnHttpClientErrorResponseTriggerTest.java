package com.davidrandoll.automation.engine.http.modules.triggers.on_http_response;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.AutomationEngineTest;
import com.davidrandoll.automation.engine.http.JsonTestUtils;
import com.davidrandoll.automation.engine.http.events.AEHttpResponseEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;


class OnHttpClientErrorResponseTriggerTest extends AutomationEngineTest {


    @Test
    void testShouldTriggerOnClientErrorResponseAndMatch() {
        var yaml = """
                alias: Client error trigger match
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseStatus: 400
                actions:
                  - action: logger
                    message: client error triggered
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.BAD_REQUEST)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerOnSuccessResponse() {
        var yaml = """
                alias: Should not trigger on 200
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseStatus: 400
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldNotTriggerOnServerErrorResponse() {
        var yaml = """
                alias: Should not trigger on 500
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseStatus: 400
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldNotTriggerIfConditionFails() {
        var yaml = """
                alias: Client error condition fails
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseBody:
                      message: "Unauthorized"
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = JsonTestUtils.json("""
                {
                  "message": "Different error"
                }
                """);

        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.UNAUTHORIZED) // 401
                .responseBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldTriggerOnClientErrorWithBodyMatch() {
        var yaml = """
                alias: 403 forbidden with message
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseBody:
                      error: "Forbidden"
                actions:
                  - action: logger
                    message: triggered
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = JsonTestUtils.json("""
                {
                  "error": "Forbidden"
                }
                """);

        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.FORBIDDEN) // 403
                .responseBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerIfStatusMissing() {
        var yaml = """
                alias: Missing status
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseBody:
                      message: "error"
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = JsonTestUtils.json("""
                {
                  "message": "error"
                }
                """);

        var event = AEHttpResponseEvent.builder()
                .responseBody(body)
                .build(); // no status

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }
}