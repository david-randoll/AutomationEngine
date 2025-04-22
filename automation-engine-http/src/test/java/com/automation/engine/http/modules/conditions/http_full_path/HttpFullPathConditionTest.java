package com.automation.engine.http.modules.conditions.http_full_path;

import ch.qos.logback.classic.Logger;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.creator.AutomationCreator;
import com.automation.engine.http.AutomationEngineHttpApplication;
import com.automation.engine.http.TestLogAppender;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
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
class HttpFullPathConditionTest {
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
    void testShouldMatchExactFullUrl() {
        var yaml = """
                alias: full-path-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    equals: https://example.com/api/users
                actions:
                  - action: logger
                    message: exact match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/users")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);

        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchFullUrlIfQueryParamsMismatch() {
        var yaml = """
                alias: full-path-query-param
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    equals: https://example.com/api/users
                actions:
                  - action: logger
                    message: full URL match ignoring query param
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/users?active=true")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/users?active=true")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchFullUrlIgnoringCase() {
        var yaml = """
                alias: full-path-case-insensitive
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    equals: HTTPS://EXAMPLE.COM/API/USERS
                actions:
                  - action: logger
                    message: case-insensitive full URL match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/users")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchFullUrlWithTrailingSlashDifference() {
        var yaml = """
                alias: full-path-trailing-slash
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    equals: https://example.com/api/users
                actions:
                  - action: logger
                    message: strict trailing slash match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/users/")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/users/")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchFullUrlUsingRegex() {
        var yaml = """
                alias: full-path-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    regex: https://example\\.com/api/.*
                actions:
                  - action: logger
                    message: regex match on full URL
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/products/123")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/products/123")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldMatchFullUrlUsingLikeWildcard() {
        var yaml = """
                alias: full-path-like
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    like: https://example.com/api/*
                actions:
                  - action: logger
                    message: wildcard match on full URL
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/orders/789")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/orders/789")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldHandleNullFullUrlGracefully() {
        var yaml = """
                alias: full-path-null
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    exists: true
                actions:
                  - action: logger
                    message: null full URL should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse(); // Should not crash

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl(null)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse(); // Should not crash
    }

    @Test
    void testShouldMatchFullUrlUsingInList() {
        var yaml = """
                alias: full-path-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    in:
                      - https://example.com/api/one
                      - https://example.com/api/two
                actions:
                  - action: logger
                    message: in list match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/two")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/two")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchFullUrlUsingNotInList() {
        var yaml = """
                alias: full-path-not-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    notIn:
                      - https://example.com/api/block
                      - https://example.com/api/deny
                actions:
                  - action: logger
                    message: should NOT match if in notIn list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/block")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/block")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }


    @Test
    void testShouldNotMatchFullUrlIfNotEqualsMatched() {
        var yaml = """
                alias: full-path-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    notEquals: https://example.com/api/deny
                actions:
                  - action: logger
                    message: should not match this URL
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/deny")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/deny")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchWhenFullUrlExists() {
        var yaml = """
                alias: full-path-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    exists: true
                actions:
                  - action: logger
                    message: exists true match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("https://example.com/api/ping")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl("https://example.com/api/ping")
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenFullUrlIsNullWithExistsTrue() {
        var yaml = """
                alias: full-path-null-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    exists: true
                actions:
                  - action: logger
                    message: should not match if full URL is missing
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl(null)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isFalse();
    }

    @Test
    void testShouldMatchWhenFullUrlIsNullWithExistsFalse() {
        var yaml = """
                alias: full-path-null-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpFullPath
                    exists: false
                actions:
                  - action: logger
                    message: null full URL allowed
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();

        var responseEvent = HttpResponseEvent.builder()
                .fullUrl(null)
                .build();
        var responseContext = EventContext.of(responseEvent);
        engine.publishEvent(responseContext);
        assertThat(automation.allConditionsMet(responseContext)).isTrue();
    }

}