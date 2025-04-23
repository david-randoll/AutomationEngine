package com.automation.engine.http.modules.conditions.http_response_status;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.AutomationEngineTest;
import com.automation.engine.http.event.HttpResponseEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;


class HttpResponseStatusConditionTest extends AutomationEngineTest {


    @Test
    void testResponseStatusEqualsMatch() {
        var yaml = """
                alias: status-equals-200
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 200
                actions:
                  - action: logger
                    message: should match 200 OK
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusEqualsMismatch() {
        var yaml = """
                alias: status-equals-404
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 404
                actions:
                  - action: logger
                    message: should not match wrong status
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseStatusNotEqualsMatch() {
        var yaml = """
                alias: status-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notEquals: 500
                actions:
                  - action: logger
                    message: should match if status not 500
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusInMatch() {
        var yaml = """
                alias: status-in-matching
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    in: [200, 201, 204]
                actions:
                  - action: logger
                    message: should match 200 in list
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusInMismatch() {
        var yaml = """
                alias: status-in-not-found
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    in: [301, 302]
                actions:
                  - action: logger
                    message: should not match 404
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.NO_CONTENT).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseStatusNotInMatch() {
        var yaml = """
                alias: status-not-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notIn: [400, 401]
                actions:
                  - action: logger
                    message: should match if status not in list
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusRegexMatch() {
        var yaml = """
                alias: status-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    regex: "^2\\\\d\\\\d$"
                actions:
                  - action: logger
                    message: should match any 2xx status
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.NO_CONTENT).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusExistsTrue() {
        var yaml = """
                alias: status-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: true
                actions:
                  - action: logger
                    message: should match if status exists
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusExistsTrueButMissing() {
        var yaml = """
                alias: status-missing-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: true
                actions:
                  - action: logger
                    message: should not match if status missing
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(null).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseStatusExistsFalseWithNull() {
        var yaml = """
                alias: status-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: false
                actions:
                  - action: logger
                    message: should match if status is missing
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(null).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchEqualsOK() {
        var yaml = """
                alias: status-equals-ok
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: OK
                actions:
                  - action: logger
                    message: matched OK
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchEqualsOKCaseSensitive() {
        var yaml = """
                alias: status-equals-ok-case-sensitive
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: Ok
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusInList() {
        var yaml = """
                alias: status-in-list
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    in: [OK, Created, Accepted]
                actions:
                  - action: logger
                    message: matched in list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.CREATED).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchNotIn() {
        var yaml = """
                alias: status-not-in-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notIn: [OK, Created]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.CREATED)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchNotInSuccess() {
        var yaml = """
                alias: status-not-in-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notIn: [OK]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.CREATED)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchNotInCreated() {
        var yaml = """
                alias: status-not-in-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notIn: [Created]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.CREATED)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldNotMatchInCreated() {
        var yaml = """
                alias: status-not-in-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    in: [Created]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.CREATED)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchRegexNotFound() {
        var yaml = """
                alias: status-regex-not
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    regex: "^Not.*"
                actions:
                  - action: logger
                    message: matched not.*
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.NOT_FOUND).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchRegexNumericOnOK() {
        var yaml = """
                alias: status-regex-numeric-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    regex: "^2..$"
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenStatusExists() {
        var yaml = """
                alias: status-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: true
                actions:
                  - action: logger
                    message: matched exists
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenStatusDoesNotExist() {
        var yaml = """
                alias: status-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: false
                actions:
                  - action: logger
                    message: matched missing status
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(null).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusFamily2xx() {
        var yaml = """
                alias: status-family-2xx
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 2xx
                actions:
                  - action: logger
                    message: matched 2xx family
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusFamily5xx() {
        var yaml = """
                alias: status-family-5xx
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 5xx
                actions:
                  - action: logger
                    message: matched 5xx family
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchStatusFamily2xx() {
        var yaml = """
                alias: status-family-not-2xx
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 2xx
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.NOT_FOUND)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchStatusFamilyInList() {
        var yaml = """
                alias: status-family-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    in: [2xx, 4xx]
                actions:
                  - action: logger
                    message: matched 4xx family from list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.BAD_REQUEST)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusFamilyRegex() {
        var yaml = """
                alias: status-family-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    regex: "^[45]xx$"
                actions:
                  - action: logger
                    message: matched 4xx or 5xx family
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.FORBIDDEN)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusReasonPhrase() {
        var yaml = """
                alias: status-reason-phrase
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: Not Found
                actions:
                  - action: logger
                    message: matched reason phrase
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.NOT_FOUND)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusName() {
        var yaml = """
                alias: status-name
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: NOT_FOUND
                actions:
                  - action: logger
                    message: matched status name
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.NOT_FOUND)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusToString() {
        var yaml = """
                alias: status-to-string
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 404 NOT_FOUND
                actions:
                  - action: logger
                    message: matched status string
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.NOT_FOUND)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusNumericCode() {
        var yaml = """
                alias: status-code
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: "404"
                actions:
                  - action: logger
                    message: matched status code
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.NOT_FOUND)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusInMultipleFormats() {
        var yaml = """
                alias: status-multiple-formats
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    in: ["404", "NOT_FOUND", "4xx", "Not Found"]
                actions:
                  - action: logger
                    message: matched one of the formats
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.NOT_FOUND)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldPassNotEquals() {
        var yaml = """
                alias: status-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notEquals: OK
                actions:
                  - action: logger
                    message: not OK response
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.NOT_FOUND) // 404
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldPassNotIn() {
        var yaml = """
                alias: status-not-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notIn: ["200", "201", "202"]
                actions:
                  - action: logger
                    message: not in 2xx group
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusFamilyWithRegex() {
        var yaml = """
                alias: status-family-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    regex: ^4..$
                actions:
                  - action: logger
                    message: client error
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.BAD_REQUEST) // 400
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldFailOnWrongEquals() {
        var yaml = """
                alias: status-fail-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: OK
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldHandleNullStatus() {
        var yaml = """
                alias: status-null
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: OK
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(null).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldPassWhenStatusExists() {
        var yaml = """
                alias: status-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: true
                actions:
                  - action: logger
                    message: status is present
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.NO_CONTENT).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldPassWhenStatusDoesNotExist() {
        var yaml = """
                alias: status-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: false
                actions:
                  - action: logger
                    message: no status present
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(null).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatch5xxFamily() {
        var yaml = """
                alias: status-family-5xx
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 5xx
                actions:
                  - action: logger
                    message: server error occurred
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.GATEWAY_TIMEOUT).build(); // 504
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldFailWrongFamilyMatch() {
        var yaml = """
                alias: status-wrong-family
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 4xx
                actions:
                  - action: logger
                    message: should not log
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build(); // 200
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }


}