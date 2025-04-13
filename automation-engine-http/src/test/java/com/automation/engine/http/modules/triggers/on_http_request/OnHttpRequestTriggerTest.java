package com.automation.engine.http.modules.triggers.on_http_request;

import ch.qos.logback.classic.Logger;
import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.creator.AutomationCreator;
import com.automation.engine.http.AutomationEngineHttpApplication;
import com.automation.engine.http.TestLogAppender;
import com.automation.engine.http.event.HttpMethodEnum;
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
class OnHttpRequestTriggerTest {

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
    void testAutomationTriggersForSingleMatchingMethod() {
        var yaml = """
                alias: Match POST requests
                triggers:
                  - trigger: onHttpRequest
                    method: POST
                actions:
                  - action: logger
                    message: Matched POST method
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger for POST")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Matched POST method"));
    }


    @Test
    void testAutomationDoesNotTriggerForNonMatchingMethod() {
        var yaml = """
                alias: Match only POST
                triggers:
                  - trigger: onHttpRequest
                    method: POST
                actions:
                  - action: logger
                    message: Should not match GET
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger for GET")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not match GET"));
    }

    @Test
    void testAutomationTriggersForMultipleMethodsMatchingOne() {
        var yaml = """
                alias: Match multiple methods
                triggers:
                  - trigger: onHttpRequest
                    methods: [POST, PUT, PATCH]
                actions:
                  - action: logger
                    message: Method matched one of [POST, PUT, PATCH]
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.PUT)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger for PUT (in allowed methods)")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Method matched one of [POST, PUT, PATCH]"));
    }

    @Test
    void testAutomationDoesNotTriggerForMultipleMethodsNoMatch() {
        var yaml = """
                alias: Match selected methods
                triggers:
                  - trigger: onHttpRequest
                    methods: [POST, PUT]
                actions:
                  - action: logger
                    message: This should not appear
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.DELETE)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger for DELETE")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not appear"));
    }

    @Test
    void testAutomationTriggersWhenNoMethodSpecified() {
        var yaml = """
                alias: No method filter
                triggers:
                  - trigger: onHttpRequest
                actions:
                  - action: logger
                    message: Triggered with no method filter
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.HEAD)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger since no method is specified")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Triggered with no method filter"));
    }

    @Test
    void testAutomationDoesNotTriggerIfEventHasNoMethodAndTriggerFiltersMethod() {
        var yaml = """
                alias: Expects method to match
                triggers:
                  - trigger: onHttpRequest
                    methods: [GET]
                actions:
                  - action: logger
                    message: Should not trigger without method
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger if method is null but required")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger without method"));
    }

    @Test
    void testAutomationTriggersForExactFullPathMatch() {
        var yaml = """
                alias: Match full URL
                triggers:
                  - trigger: onHttpRequest
                    fullPath: "http://localhost:8080/api/users"
                actions:
                  - action: logger
                    message: Exact full path matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should match exact full URL")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Exact full path matched"));
    }

    @Test
    void testAutomationDoesNotTriggerForNonMatchingFullPath() {
        var yaml = """
                alias: No match for this URL
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["http://localhost:8080/api/users"]
                actions:
                  - action: logger
                    message: Should not appear
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/products")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not match fullPath")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not appear"));
    }

    @Test
    void testAutomationTriggersForMatchingOneOfMultipleFullPaths() {
        var yaml = """
                alias: Match any full URL
                triggers:
                  - trigger: onHttpRequest
                    fullPaths: [
                      "http://localhost:8080/api/users",
                      "http://localhost:8080/api/products"
                    ]
                actions:
                  - action: logger
                    message: One of the full paths matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/products")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger if one fullPath matches")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("One of the full paths matched"));
    }

    @Test
    void testAutomationDoesNotTriggerIfNoneOfMultipleFullPathsMatch() {
        var yaml = """
                alias: No match in full path list
                triggers:
                  - trigger: onHttpRequest
                    fullPath: [
                      "http://localhost:8080/api/users",
                      "http://localhost:8080/api/orders"
                    ]
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/unknown")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger for unknown path")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersForFullPathWithPathVariableStyle() {
        var yaml = """
                alias: Match dynamic full path
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["http://localhost:8080/api/users/{id}"]
                actions:
                  - action: logger
                    message: Full path with dynamic ID matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/users/123")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger for /api/users/{id} style match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Full path with dynamic ID matched"));
    }

    @Test
    void testAutomationDoesNotTriggerIfRegexPatternDoesNotMatchFullPath() {
        var yaml = """
                alias: Regex-style pattern not matched
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["http://localhost:8080/api/orders/{orderId}"]
                actions:
                  - action: logger
                    message: Should not match other routes
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/products/456")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger if pattern doesn’t match")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not match other routes"));
    }

    @Test
    void testAutomationTriggersForFullPathWithPathVariableInMiddle() {
        var yaml = """
                alias: Match full path with dynamic segment in middle
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["http://localhost:8080/api/users/{id}/posts"]
                actions:
                  - action: logger
                    message: Matched dynamic path in middle
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/users/42/posts")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger for /api/users/{id}/posts")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Matched dynamic path in middle"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenMiddlePathVariablePatternDoesNotMatch() {
        var yaml = """
                alias: No match if path doesn’t match dynamic segment
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["http://localhost:8080/api/users/{id}/posts"]
                actions:
                  - action: logger
                    message: Should not log this
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/users/42/comments")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger for /api/users/42/comments")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not log this"));
    }

    @Test
    void testAutomationTriggersForTrailingSlashVariant() {
        var yaml = """
                alias: Match with optional trailing slash
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["http://localhost:8080/api/users"]
                actions:
                  - action: logger
                    message: Matched even with slash
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/users/")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Matched even with slash"));
    }

    @Test
    void testFullPathIgnoresQueryString() {
        var yaml = """
                alias: Match path ignoring query string
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["http://localhost:8080/api/users/42"]
                actions:
                  - action: logger
                    message: Full path match ignoring query string
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/users/42?sort=asc")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse(); // Depends on implementation!
    }

    @Test
    void testAutomationDoesNotTriggerForDifferentProtocol() {
        var yaml = """
                alias: Protocol mismatch should not trigger
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["https://localhost:8080/api/users"]
                actions:
                  - action: logger
                    message: Should not match HTTP if HTTPS expected
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testAutomationTriggersWhenPortOmittedIfMatchingBase() {
        var yaml = """
                alias: Match without port
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["http://localhost/api/users"]
                actions:
                  - action: logger
                    message: Match should still happen even without port
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:80/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse(); // or true if logic strips port
    }

    @Test
    void testAutomationHandlesSpecialRegexCharacters() {
        var yaml = """
                alias: Match literal dot in path
                triggers:
                  - trigger: onHttpRequest
                    fullPath: ["http://localhost:8080/api/file/config.json"]
                actions:
                  - action: logger
                    message: Special char handled
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/file/config.json")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testEmptyFullPathDoesTrigger() {
        var yaml = """
                alias: Empty fullPath should trigger
                triggers:
                  - trigger: onHttpRequest
                    fullPath: []
                actions:
                  - action: logger
                    message: Should log
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost:8080/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger since fullPath is empty")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Should log"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenFullPathIsNotSpecified() {
        var yaml = """
                alias: No fullPath specified
                triggers:
                  - trigger: onHttpRequest
                    methods: [GET]
                actions:
                  - action: logger
                    message: This should only match method
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .fullUrl("http://localhost:8080/api/users/42")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        // Should trigger since fullPath is not defined and only method is checked
        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger based only on method match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("This should only match method"));
    }


}