package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_path;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.AutomationEngineTest;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class HttpPathConditionTest extends AutomationEngineTest {


    @Test
    void testShouldTriggerWhenHttpPathEquals() {
        var yaml = """
                alias: path-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    equals: /api/users
                actions:
                  - action: logger
                    message: path matched /api/users
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/api/users")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldTriggerWhenHttpPathNotEquals() {
        var yaml = """
                alias: path-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    notEquals: /api/products
                actions:
                  - action: logger
                    message: path is not /api/products
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/api/users")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldTriggerWhenHttpPathInList() {
        var yaml = """
                alias: path-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    in: [/api/users, /api/products]
                actions:
                  - action: logger
                    message: path in list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/api/products")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/api/products")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldTriggerWhenHttpPathNotInList() {
        var yaml = """
                alias: path-not-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    notIn: [/forbidden, /private]
                actions:
                  - action: logger
                    message: path not in blocked list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/public/data")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/public/data")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldTriggerWhenHttpPathMatchesRegex() {
        var yaml = """
                alias: path-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    regex: "/api/users/\\\\d+"
                actions:
                  - action: logger
                    message: path matches regex
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/api/users/123")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/api/users/123")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldTriggerWhenHttpPathMatchesWildcardLike() {
        var yaml = """
                alias: path-like
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    like: /api/*
                actions:
                  - action: logger
                    message: path matches wildcard
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/api/users")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldTriggerWhenHttpPathExists() {
        var yaml = """
                alias: path-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    exists: true
                actions:
                  - action: logger
                    message: path exists
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/some/path")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/some/path")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotTriggerWhenHttpPathIsNullAndExistsTrue() {
        var yaml = """
                alias: path-null-check
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    exists: true
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .path(null)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldNotTriggerWhenPathHasTrailingSlashMismatch() {
        var yaml = """
                alias: path-trailing-slash
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    equals: /api/users
                actions:
                  - action: logger
                    message: path equals without slash
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/api/users/")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/api/users/")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchPathIgnoringCase() {
        var yaml = """
                alias: path-case-insensitive
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    equals: /API/USERS
                actions:
                  - action: logger
                    message: case-insensitive match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/api/users")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchPathWithQueryStringWhenEqualsUsed() {
        var yaml = """
                alias: path-query-string
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    equals: /api/users
                actions:
                  - action: logger
                    message: strict path match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/api/users?active=true")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/api/users?active=true")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchUrlEncodedPath() {
        var yaml = """
                alias: path-url-encoded
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    equals: /api/users/space test
                actions:
                  - action: logger
                    message: URL decoded match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("/api/users/space%20test")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        // This assumes no URL decoding is applied before matching
        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = AEHttpResponseEvent.builder()
                .path("/api/users/space%20test")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldNotTriggerWhenPathIsEmpty() {
        var yaml = """
                alias: path-empty
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    exists: true
                actions:
                  - action: logger
                    message: empty path check
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path("")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse(); // Empty string still ""

        var responseEvent = AEHttpResponseEvent.builder()
                .path("")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse(); // Empty string still ""
    }

    @Test
    void testShouldNotCrashWhenPathIsNullAndRegexUsed() {
        var yaml = """
                alias: path-null-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpPath
                    regex: /api/.*
                actions:
                  - action: logger
                    message: null path
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .path(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse(); // Should not throw

        var responseEvent = AEHttpResponseEvent.builder()
                .path(null)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse(); // Should not throw
    }


}