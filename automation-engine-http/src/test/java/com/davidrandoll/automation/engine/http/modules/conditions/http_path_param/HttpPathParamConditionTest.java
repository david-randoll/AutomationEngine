package com.davidrandoll.automation.engine.http.modules.conditions.http_path_param;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.AutomationEngineTest;
import com.davidrandoll.automation.engine.http.event.HttpRequestEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class HttpPathParamConditionTest extends AutomationEngineTest {


    @Test
    void testShouldMatchWhenPathParamEquals() {
        var yaml = """
                alias: path-param-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      userId:
                        equals: 123
                actions:
                  - action: logger
                    message: matched on equals
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("userId", "123"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenPathParamEqualsFails() {
        var yaml = """
                alias: path-param-equals-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      userId:
                        equals: 123
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("userId", "456"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchWhenPathParamNotEquals() {
        var yaml = """
                alias: path-param-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      userId:
                        notEquals: admin
                actions:
                  - action: logger
                    message: userId is not admin
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("userId", "guest"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenPathParamIn() {
        var yaml = """
                alias: path-param-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      role:
                        in: [admin, editor, viewer]
                actions:
                  - action: logger
                    message: valid role
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("role", "editor"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenPathParamNotIn() {
        var yaml = """
                alias: path-param-in-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      role:
                        in: [admin, editor]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("role", "guest"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchWhenPathParamNotInList() {
        var yaml = """
                alias: path-param-not-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      status:
                        notIn: [banned, inactive]
                actions:
                  - action: logger
                    message: active user
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("status", "active"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenPathParamExistsTrue() {
        var yaml = """
                alias: path-param-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      sessionId:
                        exists: true
                actions:
                  - action: logger
                    message: session exists
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("sessionId", "abc123"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenPathParamExistsFalse() {
        var yaml = """
                alias: path-param-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      sessionId:
                        exists: false
                actions:
                  - action: logger
                    message: session not present
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of())
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenPathParamRegexMatches() {
        var yaml = """
                alias: path-param-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      orderId:
                        regex: "^[A-Z]{2}-[0-9]{4}$"
                actions:
                  - action: logger
                    message: orderId matches format
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("orderId", "AB-2024"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenPathParamRegexFails() {
        var yaml = """
                alias: path-param-regex-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      orderId:
                        regex: "^[A-Z]{2}-[0-9]{4}$"
                actions:
                  - action: logger
                    message: invalid orderId
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("orderId", "123-AB"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchWithMultiplePathParamsAllConditionsMet() {
        var yaml = """
                alias: path-param-multiple
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      userId:
                        equals: "u123"
                      role:
                        in: ["admin", "editor"]
                      status:
                        notEquals: "banned"
                actions:
                  - action: logger
                    message: all params valid
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of(
                        "userId", "u123",
                        "role", "admin",
                        "status", "active"
                ))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenOneParamFailsInCheck() {
        var yaml = """
                alias: path-param-multiple-fail-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      userId:
                        equals: "u123"
                      role:
                        in: ["admin", "editor"]
                      status:
                        notEquals: "banned"
                actions:
                  - action: logger
                    message: one failed
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of(
                        "userId", "u123",
                        "role", "viewer",  // not in list
                        "status", "active"
                ))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchWithMixedConditions() {
        var yaml = """
                alias: path-param-mixed
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      sessionId:
                        exists: true
                      trackingId:
                        regex: "^track-[0-9]+$"
                      userType:
                        equals: "guest"
                actions:
                  - action: logger
                    message: all matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of(
                        "sessionId", "xyz",
                        "trackingId", "track-12345",
                        "userType", "guest"
                ))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenParamMissingButExistsTrue() {
        var yaml = """
                alias: path-param-missing-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      token:
                        exists: true
                      env:
                        equals: "prod"
                actions:
                  - action: logger
                    message: missing token should fail
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("env", "prod")) // token missing
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchWhenParamMissingAndExistsFalse() {
        var yaml = """
                alias: path-param-exists-false-mix
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      debug:
                        exists: false
                      version:
                        in: ["v1", "v2"]
                actions:
                  - action: logger
                    message: debug param absent
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("version", "v1"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenParamExistsButShouldnt() {
        var yaml = """
                alias: path-param-exists-false-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPathParam
                    pathParams:
                      debug:
                        exists: false
                actions:
                  - action: logger
                    message: debug should not exist
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .pathParams(Map.of("debug", "true"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

}