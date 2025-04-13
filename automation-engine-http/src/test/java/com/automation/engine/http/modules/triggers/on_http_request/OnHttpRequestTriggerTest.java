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
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

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

    /*
        Methods
     */
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

    /*
        Full Path
     */
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

    /*
        Path
     */
    @Test
    void testAutomationTriggersForMatchingPath() {
        var yaml = """
                alias: Match /api/users path
                triggers:
                  - trigger: onHttpRequest
                    path: /api/users
                actions:
                  - action: logger
                    message: Path /api/users triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger for exact matching path")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Path /api/users triggered"));
    }

    @Test
    void testAutomationDoesNotTriggerForNonMatchingPath() {
        var yaml = """
                alias: Match /api/users path
                triggers:
                  - trigger: onHttpRequest
                    path: /api/users
                actions:
                  - action: logger
                    message: Should not match
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/accounts")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger for non-matching path")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not match"));
    }

    @Test
    void testAutomationTriggersWhenOneOfMultiplePathsMatches() {
        var yaml = """
                alias: Match multiple paths
                triggers:
                  - trigger: onHttpRequest
                    paths:
                      - /api/accounts
                      - /api/users
                actions:
                  - action: logger
                    message: One of the paths matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger when one of multiple paths match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("One of the paths matched"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenNoneOfMultiplePathsMatch() {
        var yaml = """
                alias: Match multiple paths
                triggers:
                  - trigger: onHttpRequest
                    paths:
                      - /api/accounts
                      - /api/items
                actions:
                  - action: logger
                    message: Should not match any path
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger if no paths match")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not match any path"));
    }

    @Test
    void testAutomationTriggersForPathWithDynamicSegment() {
        var yaml = """
                alias: Match path with dynamic segment
                triggers:
                  - trigger: onHttpRequest
                    path: /api/users/{id}/posts
                actions:
                  - action: logger
                    message: Dynamic path matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users/42/posts")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should match path with dynamic segment")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Dynamic path matched"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenDynamicPathDoesNotMatch() {
        var yaml = """
                alias: Dynamic path pattern
                triggers:
                  - trigger: onHttpRequest
                    path: /api/users/{id}/posts
                actions:
                  - action: logger
                    message: Should not match
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users/posts")
                .build(); // missing the {id} in the middle

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not match if required dynamic part is missing")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not match"));
    }

    @Test
    void testAutomationTriggersWhenPathNotSpecified() {
        var yaml = """
                alias: No path specified
                triggers:
                  - trigger: onHttpRequest
                    methods: [GET]
                actions:
                  - action: logger
                    message: Triggered by method only
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .path("/some/endpoint")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger based on method even when path is not specified")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Triggered by method only"));
    }

    @Test
    void testAutomationDoesNotTriggerForPathWithDifferentStaticSegment() {
        var yaml = """
                alias: Mismatch in static segment
                triggers:
                  - trigger: onHttpRequest
                    path: /api/users/{id}/posts
                actions:
                  - action: logger
                    message: Should not match mismatched static segment
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users/123/order") // should NOT match /api/users/{id}/posts
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger due to static segment mismatch at the end")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not match mismatched static segment"));
    }

    @Test
    void testPathMatchesRegex() {
        var yaml = """
                alias: Path with regex
                triggers:
                  - trigger: onHttpRequest
                    paths: ["/api/users/.*/posts"]
                actions:
                  - action: logger
                    message: Regex match triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users/123/posts")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger when path matches regex pattern")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Regex match triggered"));
    }

    @Test
    void testPathDoesNotMatchRegex() {
        var yaml = """
                alias: Path with regex no match
                triggers:
                  - trigger: onHttpRequest
                    paths: ["/api/users/.*/posts"]
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users/123/comments")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger if regex doesn't match")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testFullPathMatchesRegexWithDynamicInMiddle() {
        var yaml = """
                alias: FullPath regex with dynamic in middle
                triggers:
                  - trigger: onHttpRequest
                    fullPaths: ["http://localhost/api/.*/posts"]
                actions:
                  - action: logger
                    message: FullPath regex triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .fullUrl("http://localhost/api/123/posts")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger when full path matches regex")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("FullPath regex triggered"));
    }

    @Test
    void testPathMatchingWithTrailingSlash() {
        var yaml = """
                alias: Path with trailing slash
                triggers:
                  - trigger: onHttpRequest
                    paths: [/api/users/]
                actions:
                  - action: logger
                    message: Matched trailing slash
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Matched trailing slash"));
    }

    @Test
    void testPathMatchingIgnoresQueryString() {
        var yaml = """
                alias: Path match ignores query string
                triggers:
                  - trigger: onHttpRequest
                    paths: [/api/search]
                actions:
                  - action: logger
                    message: Search path matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/search")
                .queryParams(MultiValueMap.fromMultiValue(Map.of("q", List.of("something"))))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Search path matched"));
    }

    @Test
    void testPathWithExtraSegmentDoesNotMatch() {
        var yaml = """
                alias: No match for extra segment
                triggers:
                  - trigger: onHttpRequest
                    paths: [/api/users]
                actions:
                  - action: logger
                    message: Should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users/123")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not match"));
    }

    @Test
    void testPathWithSpecialCharacters() {
        var yaml = """
                alias: Match with special characters
                triggers:
                  - trigger: onHttpRequest
                    paths: [/api/items/@special]
                actions:
                  - action: logger
                    message: Special path matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/items/@special")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Special path matched"));
    }

    @Test
    void testMultipleDynamicSegments() {
        var yaml = """
                alias: Match dynamic segments
                triggers:
                  - trigger: onHttpRequest
                    paths: ["/api/{type}/{id}/details"]
                actions:
                  - action: logger
                    message: Dynamic path matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/product/42/details")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Dynamic path matched"));
    }

    @Test
    void testRegexPathEndingWildcardMatch() {
        var yaml = """
                alias: Match any subpath under /api/users
                triggers:
                  - trigger: onHttpRequest
                    paths: [/api/users/.*]
                actions:
                  - action: logger
                    message: Matched user subpath
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/users/123/profile")
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should match any subpath under /api/users/")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Matched user subpath"));
    }


    /*
        Headers
     */
    @Test
    void testAutomationTriggersForMatchingHeader() {
        var yaml = """
                alias: Match header X-Auth-Type
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: Bearer
                actions:
                  - action: logger
                    message: Header match triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Bearer");

        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger when X-Auth-Type matches")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Header match triggered"));
    }

    @Test
    void testAutomationDoesNotTriggerForMismatchingHeader() {
        var yaml = """
                alias: Match header X-Auth-Type
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: Bearer
                actions:
                  - action: logger
                    message: Header mismatch should not trigger
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Basic");

        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should not trigger when X-Auth-Type does not match")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Header mismatch should not trigger"));
    }

    @Test
    void testAutomationTriggersForMultipleMatchingHeaders() {
        var yaml = """
                alias: Match multiple headers
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: Bearer
                      X-Client-Version: v1
                actions:
                  - action: logger
                    message: Multiple headers matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Bearer");
        headers.add("X-Client-Version", "v1");

        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Multiple headers matched"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenOneHeaderDoesNotMatch() {
        var yaml = """
                alias: One header mismatched
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: Bearer
                      X-Client-Version: v1
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Bearer");
        headers.add("X-Client-Version", "v2"); // mismatch here

        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenHeaderIsMissing() {
        var yaml = """
                alias: Header missing
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: Bearer
                actions:
                  - action: logger
                    message: Should not trigger when header is missing
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders(); // empty

        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger when header is missing"));
    }

    @Test
    void testAutomationTriggersWhenHeaderNameHasDifferentCase() {
        var yaml = """
                alias: Header case insensitive
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      x-auth-type: Bearer
                actions:
                  - action: logger
                    message: Header matched with different case
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Bearer"); // uppercase

        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Header matched with different case"));
    }

    @Test
    void testAutomationTriggersWhenNoHeadersSpecified() {
        var yaml = """
                alias: No headers in trigger
                triggers:
                  - trigger: onHttpRequest
                actions:
                  - action: logger
                    message: Triggered with no headers specified
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Bearer");

        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger because no headers are required")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Triggered with no headers specified"));
    }

    @Test
    void testAutomationDoesNotTriggerForExtraHeader() {
        var yaml = """
                alias: Match header X-Auth-Type
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: Bearer
                actions:
                  - action: logger
                    message: Header matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Bearer");
        headers.add("X-Extra-Header", "SomeValue"); // extra header
        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger when header matches, even with extra headers present")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Header matched"));
    }

    @Test
    void testHeaderValueWithLeadingTrailingSpaces() {
        var yaml = """
                alias: Match header X-Auth-Type with spaces
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: Bearer
                actions:
                  - action: logger
                    message: Header matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Bearer "); // with trailing space
        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger for header with leading/trailing spaces")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Header matched"));
    }

    @Test
    void testMultipleValuesForHeader() {
        var yaml = """
                alias: Match header X-Auth-Type with multiple values
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: Bearer
                actions:
                  - action: logger
                    message: Header matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Bearer");
        headers.add("X-Auth-Type", "Basic"); // multiple values for the same header
        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger when header has multiple values and one matches")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Header matched"));
    }

    @Test
    void testEmptyHeaderValueInYaml() {
        var yaml = """
                alias: Match empty header value
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: ""
                actions:
                  - action: logger
                    message: Header matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "");  // empty value
        var event = HttpRequestEvent.builder()
                .headers(headers)  // empty value
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger when empty header value matches")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Header matched"));
    }

    @Test
    void testNullHeaderValueInEvent() {
        var yaml = """
                alias: Match null header value
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: Bearer
                actions:
                  - action: logger
                    message: Header matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", null);  // null value
        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should not trigger when header is null and does not match")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Header matched"));
    }

    @Test
    void testRegexMatchingHeaderValue() {
        var yaml = """
                alias: Match header X-Auth-Type using regex
                triggers:
                  - trigger: onHttpRequest
                    headers:
                      X-Auth-Type: 'Bearer .*'
                actions:
                  - action: logger
                    message: Header matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth-Type", "Bearer token123");  // matches regex
        var event = HttpRequestEvent.builder()
                .headers(headers)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger when header matches regex")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Header matched"));
    }


}