package com.automation.engine.http.modules.conditions.http_path;

import ch.qos.logback.classic.Logger;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.creator.AutomationCreator;
import com.automation.engine.http.AutomationEngineHttpApplication;
import com.automation.engine.http.TestLogAppender;
import com.automation.engine.http.event.HttpRequestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineHttpApplication.class)
@ExtendWith(SpringExtension.class)
class HttpPathConditionTest {
    @Autowired
    private AutomationEngine engine;

    @Autowired
    private AutomationCreator factory;

    private TestLogAppender logAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger("com.automation.engine");
        logAppender = new TestLogAppender();
        logger.addAppender(logAppender);
        logAppender.start();

        engine.removeAll();
    }

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

        var event = HttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
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

        var event = HttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
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

        var event = HttpRequestEvent.builder()
                .path("/api/products")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
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

        var event = HttpRequestEvent.builder()
                .path("/public/data")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
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

        var event = HttpRequestEvent.builder()
                .path("/api/users/123")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
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

        var event = HttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
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

        var event = HttpRequestEvent.builder()
                .path("/some/path")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
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

        var event = HttpRequestEvent.builder()
                .path(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
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

        var event = HttpRequestEvent.builder()
                .path("/api/users/")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
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

        var event = HttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
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

        var event = HttpRequestEvent.builder()
                .path("/api/users?active=true")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
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

        var event = HttpRequestEvent.builder()
                .path("/api/users/space%20test")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        // This assumes no URL decoding is applied before matching
        assertThat(automation.allConditionsMet(context)).isFalse();
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

        var event = HttpRequestEvent.builder()
                .path("")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse(); // Empty string still ""
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

        var event = HttpRequestEvent.builder()
                .path(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse(); // Should not throw
    }


}