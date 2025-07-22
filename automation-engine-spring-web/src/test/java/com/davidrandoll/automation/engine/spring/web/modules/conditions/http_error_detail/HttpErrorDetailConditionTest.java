package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_error_detail;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.AutomationEngineTest;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HttpErrorDetailConditionTest extends AutomationEngineTest {

    @Test
    void testShouldMatchErrorDetailEquals() {
        var yaml = """
                alias: error-detail-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpErrorDetail
                    errorDetail:
                      message:
                        equals: Invalid input
                actions:
                  - action: logger
                    message: matched error detail
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .errorDetail(Map.of("message", "Invalid input"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchErrorDetailEqualsWrong() {
        var yaml = """
                alias: error-detail-equals-wrong
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpErrorDetail
                    errorDetail:
                      message:
                        equals: Invalid input
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .errorDetail(Map.of("message", "Something else"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchErrorDetailInList() {
        var yaml = """
                alias: error-detail-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpErrorDetail
                    errorDetail:
                      message:
                        in: [Invalid input, Missing token]
                actions:
                  - action: logger
                    message: matched one of the expected errors
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .errorDetail(Map.of("message", "Missing token"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchErrorDetailNotInFail() {
        var yaml = """
                alias: error-detail-not-in-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpErrorDetail
                    errorDetail:
                      type:
                        notIn: [VALIDATION_ERROR, TOKEN_EXPIRED]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .errorDetail(Map.of("type", "TOKEN_EXPIRED"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchErrorDetailRegex() {
        var yaml = """
                alias: error-detail-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpErrorDetail
                    errorDetail:
                      code:
                        regex: "^MISSING.*"
                actions:
                  - action: logger
                    message: matched regex on code
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .errorDetail(Map.of("code", "MISSING_FIELD"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchErrorDetailExistsTrue() {
        var yaml = """
                alias: error-detail-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpErrorDetail
                    errorDetail:
                      traceId:
                        exists: true
                actions:
                  - action: logger
                    message: traceId exists
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .errorDetail(Map.of("traceId", "abc123"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchErrorDetailExistsFalse() {
        var yaml = """
                alias: error-detail-missing-field
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpErrorDetail
                    errorDetail:
                      stack:
                        exists: false
                actions:
                  - action: logger
                    message: stack trace not present
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpResponseEvent.builder()
                .errorDetail(Map.of("message", "Unauthorized"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }


}