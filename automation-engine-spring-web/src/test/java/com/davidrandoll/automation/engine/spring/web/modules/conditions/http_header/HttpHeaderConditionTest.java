package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_header;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.AutomationEngineTest;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class HttpHeaderConditionTest extends AutomationEngineTest {

    @Test
    void testShouldMatchHeaderUsingEqualsCondition() {
        var yaml = """
                alias: header-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        equals: 1234
                actions:
                  - action: logger
                    message: header matches X-Request-Id equals 1234
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "1234");
        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchHeaderUsingNotEqualsCondition() {
        var yaml = """
                alias: header-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        notEquals: 1234
                actions:
                  - action: logger
                    message: header does not match X-Request-Id notEquals 1234
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "1234");
        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchHeaderUsingInList() {
        var yaml = """
                alias: header-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        in:
                          - 1234
                          - 5678
                actions:
                  - action: logger
                    message: header matches X-Request-Id in list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "5678");
        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchHeaderUsingNotInList() {
        var yaml = """
                alias: header-not-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        notIn:
                          - 1234
                          - 5678
                actions:
                  - action: logger
                    message: header does not match X-Request-Id notIn list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "5678");
        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchHeaderUsingRegex() {
        var yaml = """
                alias: header-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        regex: "^12.*$"
                actions:
                  - action: logger
                    message: header matches X-Request-Id regex
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "1234");
        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldMatchHeaderUsingLikePattern() {
        var yaml = """
                alias: header-like
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        like: "12*"
                actions:
                  - action: logger
                    message: header matches X-Request-Id like pattern
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "123456");
        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldMatchWhenHeaderExists() {
        var yaml = """
                alias: header-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        exists: true
                actions:
                  - action: logger
                    message: header exists match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "1234");
        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenHeaderMissingWithExistsTrue() {
        var yaml = """
                alias: header-missing-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        exists: true
                actions:
                  - action: logger
                    message: should not match if header is missing
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        var event = AEHttpRequestEvent.builder()
                .headers(headers)  // Empty headers
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchWhenHeaderIsMissingWithExistsFalse() {
        var yaml = """
                alias: header-missing-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        exists: false
                actions:
                  - action: logger
                    message: should match if header is missing
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        var event = AEHttpRequestEvent.builder()
                .headers(headers)  // Empty headers
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldMatchWhenHeaderExistsAndConditionIsTrue() {
        var yaml = """
                alias: header-present-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        exists: true
                actions:
                  - action: logger
                    message: should match if header is present
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "12345");

        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenHeaderIsMissingAndExistsTrue() {
        var yaml = """
                alias: header-missing-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        exists: true
                actions:
                  - action: logger
                    message: should not match if header is missing
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders(); // Empty headers

        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchWhenHeaderValueEqualsCondition() {
        var yaml = """
                alias: header-equals-condition
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        equals: 12345
                actions:
                  - action: logger
                    message: should match if header value equals 12345
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "12345");

        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenHeaderValueDoesNotEqualCondition() {
        var yaml = """
                alias: header-not-equals-condition
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        equals: 12345
                actions:
                  - action: logger
                    message: should not match if header value is not 12345
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "67890");

        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchWhenHeaderValueIsInList() {
        var yaml = """
                alias: header-in-condition
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        in:
                          - 12345
                          - 67890
                actions:
                  - action: logger
                    message: should match if header value is in the list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "12345");

        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenHeaderValueIsNotInList() {
        var yaml = """
                alias: header-not-in-condition
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        in:
                          - 12345
                          - 67890
                actions:
                  - action: logger
                    message: should not match if header value is not in the list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "99999");

        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchWhenHeaderValueMatchesRegex() {
        var yaml = """
                alias: header-regex-condition
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        regex: "^\\\\d{5}$"
                actions:
                  - action: logger
                    message: should match if header value matches regex
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "12345");

        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenHeaderValueDoesNotMatchRegex() {
        var yaml = """
                alias: header-not-regex-condition
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpHeader
                    headers:
                      X-Request-Id:
                        regex: "^\\\\d{5}$"
                actions:
                  - action: logger
                    message: should not match if header value does not match regex
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "abcde");

        var event = AEHttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .headers(headers)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

}