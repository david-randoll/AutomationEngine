package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_response_body;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.AutomationEngineTest;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import org.junit.jupiter.api.Test;

import static com.davidrandoll.automation.engine.spring.web.JsonTestUtils.json;
import static org.assertj.core.api.Assertions.assertThat;


class HttpResponseBodyConditionTest extends AutomationEngineTest {


    @Test
    void testResponseBodyEqualsMatch() {
        var yaml = """
                alias: response-body-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      status:
                        equals: success
                actions:
                  - action: logger
                    message: response status is success
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "status": "success"
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseBodyNotEqualsMismatch() {
        var yaml = """
                alias: response-body-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      status:
                        notEquals: success
                actions:
                  - action: logger
                    message: response status should not be success
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "status": "success"
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseBodyRegexMatch() {
        var yaml = """
                alias: response-body-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      status:
                        regex: succ.*
                actions:
                  - action: logger
                    message: matches regex succ.*
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "status": "success"
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseBodyInList() {
        var yaml = """
                alias: response-body-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      status:
                        in: [success, pending]
                actions:
                  - action: logger
                    message: status is in allowed list
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "status": "pending"
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testNestedResponseBodyPathMidpointIsNotObject() {
        var yaml = """
                alias: response-body-nested-invalid
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      data.user.name.first:
                        equals: David
                actions:
                  - action: logger
                    message: should not match invalid nested path
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "data": {
                    "user": {
                      "name": "David"
                    }
                  }
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        // name is a string, can't access .first
        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseBodyLikeMatch() {
        var yaml = """
                alias: response-body-like
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      message:
                        like: "*completed successfully*"
                actions:
                  - action: logger
                    message: should match like
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "message": "Task completed successfully in 2 seconds"
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseBodyExistsTrue() {
        var yaml = """
                alias: response-body-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      user.id:
                        exists: true
                actions:
                  - action: logger
                    message: user.id exists
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "user": {
                    "id": 42
                  }
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseBodyExistsFalseButKeyPresent() {
        var yaml = """
                alias: response-body-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      user.id:
                        exists: false
                actions:
                  - action: logger
                    message: user.id should not exist
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "user": {
                    "id": 42
                  }
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseBodyDeepNestedPath() {
        var yaml = """
                alias: response-body-nested-deep
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      meta.result.data.payload.status.code:
                        equals: 200
                actions:
                  - action: logger
                    message: should match deep nested status code
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "meta": {
                    "result": {
                      "data": {
                        "payload": {
                          "status": {
                            "code": 200
                          }
                        }
                      }
                    }
                  }
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseBodyDeepPathMissing() {
        var yaml = """
                alias: response-body-deep-missing
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      meta.result.payload.status.code:
                        equals: 200
                actions:
                  - action: logger
                    message: should not match, missing some nested keys
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "meta": {
                    "result": {}
                  }
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseBodyListContainsValue() {
        var yaml = """
                alias: response-body-list-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      tags:
                        in: [java, spring, rest]
                actions:
                  - action: logger
                    message: should match list containing value
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "tags": ["spring", "boot", "api"]
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseBodyListDoesNotContainAnyValue() {
        var yaml = """
                alias: response-body-list-not-matching
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      tags:
                        in: [graphql, grpc]
                actions:
                  - action: logger
                    message: should not match if no value in list
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("""
                {
                  "tags": ["spring", "boot", "rest"]
                }
                """);

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseBodyEmptyObjectExistsFalse() {
        var yaml = """
                alias: response-body-empty-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      meta.id:
                        exists: false
                actions:
                  - action: logger
                    message: should match if field does not exist in empty object
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("{}");

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseBodyEmptyObjectExistsTrue() {
        var yaml = """
                alias: response-body-empty-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      data:
                        exists: true
                actions:
                  - action: logger
                    message: should fail if data does not exist
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var responseBody = json("{}");

        var event = AEHttpResponseEvent.builder().responseBody(responseBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseBodyIsNull() {
        var yaml = """
                alias: response-body-null
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseBody
                    responseBody:
                      message:
                        equals: success
                actions:
                  - action: logger
                    message: should not match with null body
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = AEHttpResponseEvent.builder().responseBody(null).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }
}