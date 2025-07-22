package com.davidrandoll.automation.engine.http.modules.conditions.http_method;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.http.AutomationEngineTest;
import com.davidrandoll.automation.engine.http.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.http.events.AEHttpResponseEvent;
import com.davidrandoll.spring_web_captor.event.HttpMethodEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class HttpMethodConditionTest extends AutomationEngineTest {


    @Test
    void testShouldTriggerOnHttpSuccessWithMatchingMethodCondition() {
        var yaml = """
                alias: success-with-method-condition
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    equals: GET
                actions:
                  - action: logger
                    message: triggered due to GET + 2xx
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotTriggerOnHttpSuccessWithWrongMethod() {
        var yaml = """
                alias: success-wrong-method
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    equals: POST
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldNotTriggerWhenHttpMethodIsNull() {
        var yaml = """
                alias: null-method
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    equals: GET
                actions:
                  - action: logger
                    message: method is null
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .method(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .method(null)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void shouldTriggerWhenMethodEquals() {
        var yaml = """
                alias: equals-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    equals: GET
                actions:
                  - action: logger
                    message: method is GET
                """;

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);

        var responseEvent = AEHttpResponseEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, responseEvent);

    }

    @Test
    void shouldTriggerWhenMethodNotEquals() {
        var yaml = """
                alias: not-equals-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    notEquals: POST
                actions:
                  - action: logger
                    message: method is not POST
                """;

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);

        var responseEvent = AEHttpResponseEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();
        assertSatisfied(yaml, responseEvent);
    }

    @Test
    void shouldTriggerWhenMethodInList() {
        var yaml = """
                alias: in-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    in: [GET, POST]
                actions:
                  - action: logger
                    message: method in allowed list
                """;

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .build();

        assertSatisfied(yaml, event);

        var responseEvent = AEHttpResponseEvent.builder()
                .method(HttpMethodEnum.POST)
                .build();
        assertSatisfied(yaml, responseEvent);
    }

    @Test
    void shouldTriggerWhenMethodNotInList() {
        var yaml = """
                alias: not-in-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    notIn: [PUT, DELETE]
                actions:
                  - action: logger
                    message: method is not PUT/DELETE
                """;

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);

        var responseEvent = AEHttpResponseEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();
        assertSatisfied(yaml, responseEvent);
    }

    @Test
    void shouldTriggerWhenMethodMatchesRegex() {
        var yaml = """
                alias: regex-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    regex: "G.T"
                actions:
                  - action: logger
                    message: method matches regex
                """;

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);

        var responseEvent = AEHttpResponseEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();
        assertSatisfied(yaml, responseEvent);
    }

    @Test
    void shouldTriggerWhenMethodMatchesWildcardLike() {
        var yaml = """
                alias: like-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    like: G*
                actions:
                  - action: logger
                    message: method matches wildcard
                """;

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);

        var responseEvent = AEHttpResponseEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();
        assertSatisfied(yaml, responseEvent);
    }

    @Test
    void shouldTriggerWhenMethodExistsTrue() {
        var yaml = """
                alias: exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    exists: true
                actions:
                  - action: logger
                    message: method exists
                """;

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);

        var responseEvent = AEHttpResponseEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();
        assertSatisfied(yaml, responseEvent);
    }

    @Test
    void shouldNotTriggerWhenMethodIsNullButExistsExpected() {
        var yaml = """
                alias: exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    exists: true
                actions:
                  - action: logger
                    message: shouldn't trigger
                """;

        var event = AEHttpRequestEvent.builder()
                .method(null)
                .build();

        assertNotSatisfied(yaml, event);

        var responseEvent = AEHttpResponseEvent.builder()
                .method(null)
                .build();
        assertNotSatisfied(yaml, responseEvent);
    }


    private void assertSatisfied(String yaml, IEvent event) {
        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
        var context = EventContext.of(event);
        engine.publishEvent(context);
        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    private void assertNotSatisfied(String yaml, IEvent event) {
        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
        var context = EventContext.of(event);
        engine.publishEvent(context);
        assertThat(automation.allConditionsMet(context)).isFalse();
    }

}