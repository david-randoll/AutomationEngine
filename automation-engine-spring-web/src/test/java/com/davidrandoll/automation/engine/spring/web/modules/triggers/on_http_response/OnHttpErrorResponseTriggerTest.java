package com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_response;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class OnHttpErrorResponseTriggerTest extends AutomationEngineTest {


    @Test
    void testShouldTriggerOnErrorResponseAndMatch() {
        var yaml = """
                alias: Error response trigger match
                triggers:
                  - trigger: onHttpErrorResponse
                    errorDetail:
                      message: "Internal error"
                actions:
                  - action: logger
                    message: should trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> errorDetail = Map.of("message", "Internal error");
        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorDetail(errorDetail)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerOnSuccessResponse() {
        var yaml = """
                alias: Error trigger should not activate on 200
                triggers:
                  - trigger: onHttpErrorResponse
                    errorDetail:
                      message: "Something wrong"
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> errorDetail = Map.of("message", "Something wrong");
        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK) // 200 - success
                .errorDetail(errorDetail)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldNotTriggerOnErrorResponseWithNonMatchingErrorDetail() {
        var yaml = """
                alias: Error response with wrong detail
                triggers:
                  - trigger: onHttpErrorResponse
                    errorDetail:
                      message: "Access Denied"
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> errorDetail = Map.of("message", "Internal Server Error"); // different
        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.BAD_REQUEST)
                .errorDetail(errorDetail)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldTriggerFor500ErrorAndMatchingCondition() {
        var yaml = """
                alias: 500 error match
                triggers:
                  - trigger: onHttpErrorResponse
                    responseStatus: 500
                actions:
                  - action: logger
                    message: should trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerIfNoStatusPresent() {
        var yaml = """
                alias: Missing status
                triggers:
                  - trigger: onHttpErrorResponse
                    responseStatus: 500
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder().build(); // no status

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }


}