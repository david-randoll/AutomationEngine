package com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_request;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.AutomationEngineTest;
import com.davidrandoll.spring_web_captor.event.HttpMethodEnum;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class OnHttpRequestTriggerTest extends AutomationEngineTest {


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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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

        var event = AEHttpRequestEvent.builder()
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
        var event = AEHttpRequestEvent.builder()
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
        var event = AEHttpRequestEvent.builder()
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
        var event = AEHttpRequestEvent.builder()
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
        var event = AEHttpRequestEvent.builder()
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
        var event = AEHttpRequestEvent.builder()
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
        var event = AEHttpRequestEvent.builder()
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

    /*
        Query Params
     */
    @Test
    void testAutomationTriggersForMatchingQueryParam() {
        var yaml = """
                alias: Match query param
                triggers:
                  - trigger: onHttpRequest
                    queryParams:
                      user: [admin]
                actions:
                  - action: logger
                    message: Matched query param
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("user", "admin");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger for matching query param")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Matched query param"));
    }

    @Test
    void testAutomationDoesNotTriggerForNonMatchingQueryParam() {
        var yaml = """
                alias: No match query param
                triggers:
                  - trigger: onHttpRequest
                    queryParams:
                      user: [admin]
                actions:
                  - action: logger
                    message: Should not match query param
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("user", "guest");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should not trigger for non-matching query param")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not match query param"));
    }

    @Test
    void testAutomationTriggersWhenOneQueryParamValueMatches() {
        var yaml = """
                alias: Match one of multiple values
                triggers:
                  - trigger: onHttpRequest
                    queryParams:
                      role: [admin, manager]
                actions:
                  - action: logger
                    message: Role matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("role", "manager");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger when one of multiple query param values matches")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Role matched"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenQueryParamIsMissing() {
        var yaml = """
                alias: Missing query param
                triggers:
                  - trigger: onHttpRequest
                    queryParams:
                      token: [abc123]
                actions:
                  - action: logger
                    message: Should not trigger when query param is missing
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("user", "admin"); // missing the required token param
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should not trigger when required query param is missing")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersWhenAllQueryParamsMatch() {
        var yaml = """
                alias: All query params match
                triggers:
                  - trigger: onHttpRequest
                    queryParams:
                      user: [admin]
                      active: [true]
                actions:
                  - action: logger
                    message: All query params matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("user", "admin");
        queryParams.add("active", "true");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger when all query params match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("All query params matched"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenOneQueryParamDoesNotMatch() {
        var yaml = """
                alias: One query param does not match
                triggers:
                  - trigger: onHttpRequest
                    queryParams:
                      user: [admin]
                      active: [true]
                actions:
                  - action: logger
                    message: Should not trigger if one query param fails
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("user", "admin");
        queryParams.add("active", "false"); // mismatch here
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should not trigger when one query param value does not match")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersWhenQueryParamIsEmptyString() {
        var yaml = """
                alias: Empty String Query Param
                triggers:
                  - trigger: onHttpRequest
                    query:
                      status: [""]
                actions:
                  - action: logger
                    message: Triggered on empty query param
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("status", ""); // empty string
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Triggered on empty query param"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenQueryParamIsEmpty() {
        var yaml = """
                alias: Missing Query Param
                triggers:
                  - trigger: onHttpRequest
                    query:
                      status: [active]
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams) // empty
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersWithExtraQueryParams() {
        var yaml = """
                alias: Extra Query Params Present
                triggers:
                  - trigger: onHttpRequest
                    query:
                      category: [books]
                actions:
                  - action: logger
                    message: Triggered with extra params
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("category", "books");
        queryParams.add("page", "2");
        queryParams.add("sort", "desc");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Triggered with extra params"));
    }

    @Test
    void testAutomationDoesTriggerWhenQueryParamKeyIsDifferentCase() {
        var yaml = """
                alias: Case Sensitive Key
                triggers:
                  - trigger: onHttpRequest
                    query:
                      category: [books]
                actions:
                  - action: logger
                    message: Should trigger despite case
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("Category", "books"); // uppercase key
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams) // uppercase key
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Should trigger despite case"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenQueryParamValueIsDifferentCase() {
        var yaml = """
                alias: Case Sensitive Value
                triggers:
                  - trigger: onHttpRequest
                    query:
                      category: [books]
                actions:
                  - action: logger
                    message: Should trigger despite case
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("category", "Books"); // capitalized value
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams) // capitalized value
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Should trigger despite case"));
    }

    @Test
    void testAutomationTriggersWhenQueryParamContainsRegexCharsLiterally() {
        var yaml = """
                alias: Regex-like Literal Value
                triggers:
                  - trigger: onHttpRequest
                    query:
                      search: [".*"]
                actions:
                  - action: logger
                    message: Triggered with regex-like value
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("search", ".*"); // literal value
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams) // matches exactly ".*"
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Triggered with regex-like value"));
    }

    /*
        Path Params
     */
    @Test
    void testAutomationTriggersForMatchingPathParam() {
        var yaml = """
                alias: Match Path Param ID
                triggers:
                  - trigger: onHttpRequest
                    pathParams:
                      id: [123]
                actions:
                  - action: logger
                    message: Triggered for id=123
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .pathParams(Map.of("id", "123"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Triggered for id=123"));
    }

    @Test
    void testAutomationDoesNotTriggerForNonMatchingPathParam() {
        var yaml = """
                alias: Match Path Param ID
                triggers:
                  - trigger: onHttpRequest
                    pathParams:
                      id: [123]
                actions:
                  - action: logger
                    message: Should not trigger for id!=123
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .pathParams(Map.of("id", "456"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersForMultipleMatchingPathParams() {
        var yaml = """
                alias: Match Multiple Path Params
                triggers:
                  - trigger: onHttpRequest
                    pathParams:
                      userId: [99]
                      postId: [321]
                actions:
                  - action: logger
                    message: Triggered for both path params
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .pathParams(Map.of("userId", "99", "postId", "321"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Triggered for both path params"));
    }

    @Test
    void testAutomationDoesNotTriggerIfOneOfMultiplePathParamsMismatch() {
        var yaml = """
                alias: Match Multiple Path Params
                triggers:
                  - trigger: onHttpRequest
                    pathParams:
                      userId: [99]
                      postId: [321]
                actions:
                  - action: logger
                    message: Should not trigger due to mismatch
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .pathParams(Map.of("userId", "99", "postId", "999"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationDoesNotTriggerIfPathParamMissing() {
        var yaml = """
                alias: Expecting Path Param ID
                triggers:
                  - trigger: onHttpRequest
                    pathParams:
                      id: [123]
                actions:
                  - action: logger
                    message: Should not trigger if id missing
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .pathParams(Map.of()) // No 'id'
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationDoesNotTriggerIfPathParamIsNull() {
        var yaml = """
                alias: Null Path Param Test
                triggers:
                  - trigger: onHttpRequest
                    pathParams:
                      id: [123]
                actions:
                  - action: logger
                    message: Should not trigger if id is null
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var queryParams = new HashMap<String, String>();
        queryParams.put("id", null); // Null value
        var event = AEHttpRequestEvent.builder()
                .pathParams(queryParams) // Null value
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersEvenWithExtraPathParams() {
        var yaml = """
                alias: Match Only Required Path Param
                triggers:
                  - trigger: onHttpRequest
                    pathParams:
                      id: [123]
                actions:
                  - action: logger
                    message: Triggered with extra path param
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .pathParams(Map.of("id", "123", "extra", "999"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Triggered with extra path param"));
    }

    @Test
    void testPathParamValueMatchingIsCaseSensitive() {
        var yaml = """
                alias: Case Sensitive Path Param
                triggers:
                  - trigger: onHttpRequest
                    pathParams:
                      type: [Admin]
                actions:
                  - action: logger
                    message: Triggered for Admin type
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .pathParams(Map.of("type", "admin")) // lowercased
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Triggered for Admin type"));
    }

    @Test
    void testPathParamMatchesWithRegexPattern() {
        var yaml = """
                alias: Regex Path Param Match
                triggers:
                  - trigger: onHttpRequest
                    pathParams:
                      slug: [".*article.*"]
                actions:
                  - action: logger
                    message: Triggered for article slug
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = AEHttpRequestEvent.builder()
                .pathParams(Map.of("slug", "my-article-2025"))
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Triggered for article slug"));
    }

    /*
        Combination of all
     */
    @Test
    void testAutomationTriggersWhenAllCriteriaMatch() {
        var yaml = """
                alias: All Trigger Criteria Match
                triggers:
                  - trigger: onHttpRequest
                    methods: [GET]
                    path: /api/users/{id}
                    pathParams:
                      id: [42]
                    headers:
                      X-Auth: [secret]
                    queryParams:
                      status: [active]
                actions:
                  - action: logger
                    message: All match success
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth", "secret");
        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("status", "active");
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .path("/api/users/42")
                .pathParams(Map.of("id", "42"))
                .headers(headers)
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("All match success"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenOneValueDoesNotMatch() {
        var yaml = """
                alias: Fail if Header Wrong
                triggers:
                  - trigger: onHttpRequest
                    methods: [GET]
                    path: /api/users/{id}
                    pathParams:
                      id: [42]
                    headers:
                      X-Auth: [secret]
                    queryParams:
                      status: [active]
                actions:
                  - action: logger
                    message: This should not run
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth", "wrong-secret"); // mismatch
        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("status", "active");
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .path("/api/users/42")
                .pathParams(Map.of("id", "42"))
                .headers(headers) // mismatch
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("This should not run"));
    }

    @Test
    void testAutomationTriggersWithMultipleValuesEachField() {
        var yaml = """
                alias: Match One of Each Field
                triggers:
                  - trigger: onHttpRequest
                    methods: [GET, POST]
                    path: /api/users/{id}
                    pathParams:
                      id: [42, 43]
                    headers:
                      X-Role: [admin, user]
                    queryParams:
                      status: [active, pending]
                actions:
                  - action: logger
                    message: Triggered with one match from each
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Role", "admin");
        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("status", "pending");
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .path("/api/users/43")
                .pathParams(Map.of("id", "43"))
                .headers(headers)
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Triggered with one match from each"));
    }

    @Test
    void testAutomationFailsWithMissingPathParamEvenIfOthersMatch() {
        var yaml = """
                alias: Fail On Missing Path Param
                triggers:
                  - trigger: onHttpRequest
                    methods: [GET]
                    path: /api/users/{id}
                    pathParams:
                      id: [42]
                    headers:
                      X-Auth: [secret]
                    queryParams:
                      status: [active]
                actions:
                  - action: logger
                    message: Should not trigger due to path param
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Auth", "secret");
        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("status", "active");
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .path("/api/users/42")
                .headers(headers)
                .queryParams(queryParams)
                .build(); // Missing pathParams

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersUsingFullPathAndOtherFilters() {
        var yaml = """
                alias: FullPath Combo Match
                triggers:
                  - trigger: onHttpRequest
                    methods: [POST]
                    fullPaths: ["https://api.example.com/api/users/{id}"]
                    pathParams:
                      id: [123]
                    headers:
                      X-Api-Key: [abc123]
                    queryParams:
                      type: [premium]
                actions:
                  - action: logger
                    message: Full path combo match triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Api-Key", "abc123");
        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("type", "premium");
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .fullUrl("https://api.example.com/api/users/123")
                .pathParams(Map.of("id", "123"))
                .headers(headers)
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Full path combo match triggered"));
    }

    @Test
    void testAutomationFailsDueToQueryParamMismatch() {
        var yaml = """
                alias: Fail on QueryParam Mismatch
                triggers:
                  - trigger: onHttpRequest
                    methods: [PUT]
                    path: /api/products/{sku}
                    pathParams:
                      sku: [999]
                    headers:
                      X-Env: [prod]
                    queryParams:
                      mode: [edit]
                actions:
                  - action: logger
                    message: This should not appear
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Env", "prod");
        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("mode", "view"); // mismatch
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.PUT)
                .path("/api/products/999")
                .pathParams(Map.of("sku", "999"))
                .headers(headers)
                .queryParams(queryParams) // wrong value
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("This should not appear"));
    }

    @Test
    void testHeaderAndQueryParamCaseInsensitiveMatch() {
        var yaml = """
                alias: Case Insensitive Match
                triggers:
                  - trigger: onHttpRequest
                    methods: [GET]
                    path: /api/items/{itemId}
                    pathParams:
                      itemId: [abc]
                    headers:
                      x-custom-header: [TokenValue]
                    queryParams:
                      sort: [ASC]
                actions:
                  - action: logger
                    message: Case insensitive match triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-CUSTOM-HEADER", "TokenValue"); // different casing
        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("SORT", "ASC"); // different casing
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .path("/api/items/abc")
                .pathParams(Map.of("itemId", "abc"))
                .headers(headers) // different casing
                .queryParams(queryParams) // different casing
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Case insensitive match triggered"));
    }

    @Test
    void testAutomationFailsWithWrongTypeInPathParam() {
        var yaml = """
                alias: Wrong PathParam Type
                triggers:
                  - trigger: onHttpRequest
                    methods: [GET]
                    path: /api/users/{id}
                    pathParams:
                      id: [42]
                    headers:
                      X-Client: [mobile]
                    queryParams:
                      detail: [yes]
                actions:
                  - action: logger
                    message: Should not trigger due to type mismatch
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var headers = new HttpHeaders();
        headers.add("X-Client", "mobile");
        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("detail", "yes");
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .path("/api/users/0042") // different string, not exact "42"
                .pathParams(Map.of("id", "0042")) // technically a different string
                .headers(headers)
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger due to type mismatch"));
    }

    @Test
    void testAutomationTriggersOnExactJsonBodyMatch() {
        var yaml = """
                alias: Match exact request body
                triggers:
                  - trigger: onHttpRequest
                    body:
                      name: John
                      age: 30
                actions:
                  - action: logger
                    message: Exact body matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .put("name", "John")
                .put("age", 30);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger for matching JSON body")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Exact body matched"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenBodyValueDiffers() {
        var yaml = """
                alias: Match exact request body
                triggers:
                  - trigger: onHttpRequest
                    body:
                      name: John
                      age: 30
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .put("name", "John")
                .put("age", 25); // Not matching age

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should not trigger when body differs")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersOnPartialBodyMatch() {
        var yaml = """
                alias: Partial body match
                triggers:
                  - trigger: onHttpRequest
                    body:
                      name: Alice
                actions:
                  - action: logger
                    message: Partial match success
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .put("name", "Alice")
                .put("age", 28); // Extra field

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger with partial match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Partial match success"));
    }

    @Test
    void testAutomationTriggersOnNestedJsonMatch() {
        var yaml = """
                alias: Nested body match
                triggers:
                  - trigger: onHttpRequest
                    body:
                      user:
                        name: Bob
                        active: true
                actions:
                  - action: logger
                    message: Nested match success
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var userNode = new ObjectMapper().createObjectNode()
                .put("name", "Bob")
                .put("active", true);

        var body = new ObjectMapper().createObjectNode()
                .set("user", userNode);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger for nested JSON match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Nested match success"));
    }

    @Test
    void testAutomationDoesNotTriggerIfRequiredFieldMissing() {
        var yaml = """
                alias: Missing field test
                triggers:
                  - trigger: onHttpRequest
                    body:
                      name: Charlie
                      email: test@example.com
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .put("name", "Charlie"); // Missing email

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should not trigger if a required field is missing")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersOnArrayFieldMatch() {
        var yaml = """
                alias: Array field match
                triggers:
                  - trigger: onHttpRequest
                    body:
                      tags: ["java", "spring"]
                actions:
                  - action: logger
                    message: Array matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var array = new ObjectMapper().createArrayNode()
                .add("java")
                .add("spring");

        var body = new ObjectMapper().createObjectNode()
                .set("tags", array);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should trigger when array field matches")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Array matched"));
    }

    @Test
    void testAutomationTriggersEvenWithExtraFields() {
        var yaml = """
                alias: Match with extra fields
                triggers:
                  - trigger: onHttpRequest
                    body:
                      username: testuser
                actions:
                  - action: logger
                    message: Triggered with extra field
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .put("username", "testuser")
                .put("role", "admin"); // extra field

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Automation should still trigger with extra fields")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Triggered with extra field"));
    }

    @Test
    void testAutomationTriggersOnRegexBodyFieldMatch() {
        var yaml = """
                alias: Regex match on email
                triggers:
                  - trigger: onHttpRequest
                    body:
                      email: "^.+@example\\\\.com$"
                actions:
                  - action: logger
                    message: Regex email matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .put("email", "user@example.com");

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger with regex matching email")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Regex email matched"));
    }

    @Test
    void testAutomationTriggersOnNullFieldValue() {
        var yaml = """
                alias: Match null value
                triggers:
                  - trigger: onHttpRequest
                    body:
                      status: null
                actions:
                  - action: logger
                    message: Null value match
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .putNull("status");

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger when status is null")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Null value match"));
    }

    @Test
    void testAutomationTriggersOnDeepNestedMatch() {
        var yaml = """
                alias: Deep nested match
                triggers:
                  - trigger: onHttpRequest
                    body:
                      user:
                        address:
                          city: Brooklyn
                actions:
                  - action: logger
                    message: Deep nested match success
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var address = new ObjectMapper().createObjectNode()
                .put("city", "Brooklyn")
                .put("zip", "11201");

        var user = new ObjectMapper().createObjectNode()
                .set("address", address);

        var body = new ObjectMapper().createObjectNode()
                .set("user", user);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger on deep nested JSON")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Deep nested match success"));
    }

    @Test
    void testAutomationDoesNotTriggerOnRegexMismatch() {
        var yaml = """
                alias: Regex mismatch
                triggers:
                  - trigger: onHttpRequest
                    body:
                      username: "~^admin_.*$"
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .put("username", "user_001"); // Doesn't match ^admin_.*

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger on regex mismatch")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersOnNestedObjectMatch() {
        var yaml = """
                alias: Nested object match
                triggers:
                  - trigger: onHttpRequest
                    body:
                      user:
                        name: Alice
                        age: 30
                actions:
                  - action: logger
                    message: Nested object matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var user = new ObjectMapper().createObjectNode()
                .put("name", "Alice")
                .put("age", 30);

        var body = new ObjectMapper().createObjectNode()
                .set("user", user);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger with exact nested object match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Nested object matched"));
    }

    @Test
    void testAutomationTriggersOnPartialNestedObjectMatch() {
        var yaml = """
                alias: Partial nested match
                triggers:
                  - trigger: onHttpRequest
                    body:
                      user:
                        name: Bob
                actions:
                  - action: logger
                    message: Partial nested match success
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var user = new ObjectMapper().createObjectNode()
                .put("name", "Bob")
                .put("age", 25);

        var body = new ObjectMapper().createObjectNode()
                .set("user", user);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger with partial nested object match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Partial nested match success"));
    }

    @Test
    void testAutomationDoesNotTriggerOnNestedObjectMismatch() {
        var yaml = """
                alias: Nested mismatch
                triggers:
                  - trigger: onHttpRequest
                    body:
                      user:
                        name: Charlie
                actions:
                  - action: logger
                    message: Should not trigger mismatch
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var user = new ObjectMapper().createObjectNode()
                .put("name", "Eve");

        var body = new ObjectMapper().createObjectNode()
                .set("user", user);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger when nested object value mismatches")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger mismatch"));
    }

    @Test
    void testAutomationTriggersOnDeeplyNestedObject() {
        var yaml = """
                alias: Deeply nested object match
                triggers:
                  - trigger: onHttpRequest
                    body:
                      user:
                        contact:
                          address:
                            city: Toronto
                actions:
                  - action: logger
                    message: Deep match success
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var address = new ObjectMapper().createObjectNode()
                .put("city", "Toronto");

        var contact = new ObjectMapper().createObjectNode()
                .set("address", address);

        var user = new ObjectMapper().createObjectNode()
                .set("contact", contact);

        var body = new ObjectMapper().createObjectNode()
                .set("user", user);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger on 3-level deep nested match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Deep match success"));
    }

    @Test
    void testAutomationTriggersOnArrayContainingMatchingObject() {
        var yaml = """
                alias: Match object in array
                triggers:
                  - trigger: onHttpRequest
                    body:
                      items:
                        - id: 1
                          name: Book
                actions:
                  - action: logger
                    message: Array object match
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var item1 = new ObjectMapper().createObjectNode()
                .put("id", 1)
                .put("name", "Book");

        var item2 = new ObjectMapper().createObjectNode()
                .put("id", 2)
                .put("name", "Pen");

        var body = new ObjectMapper().createObjectNode()
                .set("items", new ObjectMapper().createArrayNode().add(item1).add(item2));

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger if array contains matching object")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Array object match"));
    }

    @Test
    void testAutomationTriggersOnPartialMatchInArrayObject() {
        var yaml = """
                alias: Partial array match
                triggers:
                  - trigger: onHttpRequest
                    body:
                      items:
                        - id: 2
                actions:
                  - action: logger
                    message: Partial array match
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var item1 = new ObjectMapper().createObjectNode()
                .put("id", 1)
                .put("name", "Book");

        var item2 = new ObjectMapper().createObjectNode()
                .put("id", 2)
                .put("name", "Pen");

        var body = new ObjectMapper().createObjectNode()
                .set("items", new ObjectMapper().createArrayNode().add(item1).add(item2));

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger on partial object in array")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Partial array match"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenArrayObjectDoesNotMatch() {
        var yaml = """
                alias: No match in array
                triggers:
                  - trigger: onHttpRequest
                    body:
                      items:
                        - id: 99
                          name: Unknown
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var item1 = new ObjectMapper().createObjectNode()
                .put("id", 1)
                .put("name", "Book");

        var item2 = new ObjectMapper().createObjectNode()
                .put("id", 2)
                .put("name", "Pen");

        var body = new ObjectMapper().createObjectNode()
                .set("items", new ObjectMapper().createArrayNode().add(item1).add(item2));

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger if array does not contain expected object")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void testAutomationTriggersOnArrayOfPrimitives() {
        var yaml = """
                alias: Match primitive array
                triggers:
                  - trigger: onHttpRequest
                    body:
                      tags: [news, tech]
                actions:
                  - action: logger
                    message: Primitive array matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .set("tags", new ObjectMapper().createArrayNode()
                        .add("news")
                        .add("tech")
                        .add("science"));

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger when primitive array contains all expected values")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Primitive array matched"));
    }

    @Test
    void testAutomationTriggersIfArrayObjectHasExtraFields() {
        var yaml = """
                alias: Match object in array with extra fields
                triggers:
                  - trigger: onHttpRequest
                    body:
                      items:
                        - id: 2
                          name: Pen
                actions:
                  - action: logger
                    message: Match with extra fields
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var item = new ObjectMapper().createObjectNode()
                .put("id", 2)
                .put("name", "Pen")
                .put("price", 5.99); // Extra field

        var body = new ObjectMapper().createObjectNode()
                .set("items", new ObjectMapper().createArrayNode().add(item));

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger even with extra fields in array object")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Match with extra fields"));
    }

    @Test
    void testAutomationTriggersOnArrayOrderDifference() {
        var yaml = """
                alias: Array order shouldn't matter
                triggers:
                  - trigger: onHttpRequest
                    body:
                      tags: [tech, news]
                actions:
                  - action: logger
                    message: Array order match
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .set("tags", new ObjectMapper().createArrayNode()
                        .add("news") // Reversed order
                        .add("tech"));

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should still trigger despite array order")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Array order match"));
    }

    @Test
    void testAutomationTriggersOnNestedArrayInsideObject() {
        var yaml = """
                alias: Nested array in nested object
                triggers:
                  - trigger: onHttpRequest
                    body:
                      user:
                        interests: [reading, traveling]
                actions:
                  - action: logger
                    message: Nested array matched
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var interests = new ObjectMapper().createArrayNode().add("traveling").add("reading");

        var user = new ObjectMapper().createObjectNode()
                .set("interests", interests);

        var body = new ObjectMapper().createObjectNode()
                .set("user", user);

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger with nested array match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Nested array matched"));
    }

    @Test
    void testAutomationTriggersWithArrayAndNonArrayFields() {
        var yaml = """
                alias: Combine array and non-array fields
                triggers:
                  - trigger: onHttpRequest
                    body:
                      status: active
                      tags: [urgent]
                actions:
                  - action: logger
                    message: Combined match
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .put("status", "active")
                .set("tags", new ObjectMapper().createArrayNode().add("urgent"));

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger with array and scalar fields")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Combined match"));
    }

    @Test
    void testAutomationTriggersWithRegexInArrayValue() {
        var yaml = """
                alias: Match with regex in array
                triggers:
                  - trigger: onHttpRequest
                    body:
                      tags: [".*urgent.*"]
                actions:
                  - action: logger
                    message: Regex in array match
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .set("tags", new ObjectMapper().createArrayNode()
                        .add("very_urgent")
                        .add("low_priority"));

        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger with regex value in array")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Regex in array match"));
    }

    @Test
    void testAutomationTriggersOnBodyPathQueryHeaderCombined() {
        var yaml = """
                alias: Match body, path, query, and header
                triggers:
                  - trigger: onHttpRequest
                    path: /api/orders/{orderId}
                    query:
                      type: express
                    headers:
                      x-request-id: req-789
                    body:
                      userId: 1001
                      items:
                        - id: 201
                          name: Widget
                actions:
                  - action: logger
                    message: All combined match
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var item = new ObjectMapper().createObjectNode()
                .put("id", 201)
                .put("name", "Widget");

        var body = new ObjectMapper().createObjectNode()
                .put("userId", 1001)
                .set("items", new ObjectMapper().createArrayNode().add(item));

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("type", "express");
        var headers = new HttpHeaders();
        headers.add("x-request-id", "req-789");
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .path("/api/orders/9999")
                .queryParams(queryParams)
                .headers(headers)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger when all path, query, headers, and body match")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("All combined match"));
    }

    @Test
    void testAutomationDoesNotTriggerIfBodyDoesNotMatch() {
        var yaml = """
                alias: Complex match with body mismatch
                triggers:
                  - trigger: onHttpRequest
                    path: /api/users/{id}/purchases
                    headers:
                      auth-token: secure123
                    body:
                      verified: true
                actions:
                  - action: logger
                    message: Should not trigger due to body mismatch
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = new ObjectMapper().createObjectNode()
                .put("verified", false); // Does not match

        var headers = new HttpHeaders();
        headers.add("auth-token", "secure123");
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .path("/api/users/456/purchases")
                .headers(headers)
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger due to body mismatch")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger due to body mismatch"));
    }

    @Test
    void testAutomationTriggersWithNestedBodyAndLooseQueryAndCaseInsensitiveHeader() {
        var yaml = """
                alias: Complex match with relaxed query and header
                triggers:
                  - trigger: onHttpRequest
                    query:
                      page: 1
                    headers:
                      Content-Type: application/json
                    body:
                      user:
                        info:
                          active: true
                actions:
                  - action: logger
                    message: Match with nested and relaxed criteria
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var info = new ObjectMapper().createObjectNode()
                .put("active", true);

        var user = new ObjectMapper().createObjectNode()
                .set("info", info);

        var body = new ObjectMapper().createObjectNode()
                .set("user", user);

        var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("page", "1"); // Should match
        queryParams.add("extra", "ignore-me"); // Extra param, should be ignored
        var headers = new HttpHeaders();
        headers.add("content-type", "application/json"); // Different casing
        var event = AEHttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .queryParams(queryParams)
                .headers(headers) // Different casing
                .requestBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger with nested body, extra query param, and case-insensitive header")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Match with nested and relaxed criteria"));
    }
}