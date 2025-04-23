package com.automation.engine.http.modules.triggers.on_http_path_exists;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.AutomationEngineTest;
import com.automation.engine.http.event.HttpMethodEnum;
import com.automation.engine.http.event.HttpRequestEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OnHttpPathExistsTriggerTest extends AutomationEngineTest {
    @Test
    void testExactPathMatch() {
        var yaml = """
                alias: path-exists-exact
                triggers:
                  - trigger: onHttpPathExists
                    path: /api/user
                actions:
                  - action: logger
                    message: exact match worked
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/user")
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testRegexPathMatch() {
        var yaml = """
                alias: path-exists-regex
                triggers:
                  - trigger: onHttpPathExists
                    path: /api/.*
                actions:
                  - action: logger
                    message: regex match worked
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/user/123")
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testEndpointDoesNotExist() {
        var yaml = """
                alias: path-exists-not-found
                triggers:
                  - trigger: onHttpPathExists
                    path: /api/missing
                actions:
                  - action: logger
                    message: should not run if endpoint missing
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/missing")
                .endpointExists(false)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testPathMismatch() {
        var yaml = """
                alias: path-exists-mismatch
                triggers:
                  - trigger: onHttpPathExists
                    path: /api/products
                actions:
                  - action: logger
                    message: should not match wrong path
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/orders")
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testNullEventPath() {
        var yaml = """
                alias: path-exists-null-event
                triggers:
                  - trigger: onHttpPathExists
                    path: /api/orders
                actions:
                  - action: logger
                    message: path should be non-null
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path(null)
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testNullTriggerPath() {
        var yaml = """
                alias: path-exists-null-trigger
                triggers:
                  - trigger: onHttpPathExists
                actions:
                  - action: logger
                    message: no path given
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/orders")
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testTriggerWithMatchingPathAndMethod() {
        var yaml = """
                alias: match-path-method
                triggers:
                  - trigger: onHttpPathExists
                    paths: [ "/api/hello" ]
                    methods: [ GET ]
                actions:
                  - action: logger
                    message: matched!
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/hello")
                .method(HttpMethodEnum.GET)
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testTriggerWithPathRegex() {
        var yaml = """
                alias: match-path-regex
                triggers:
                  - trigger: onHttpPathExists
                    paths: [ "/api/.*" ]
                    methods: [ POST ]
                actions:
                  - action: logger
                    message: regex path match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/api/item/123")
                .method(HttpMethodEnum.POST)
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testTriggerWithoutMethodsMatchesAnyMethod() {
        var yaml = """
                alias: match-no-methods
                triggers:
                  - trigger: onHttpPathExists
                    paths: [ "/test" ]
                actions:
                  - action: logger
                    message: method skipped
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/test")
                .method(HttpMethodEnum.PUT)
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testTriggerWithoutPathsMatchesAnyPath() {
        var yaml = """
                alias: match-no-paths
                triggers:
                  - trigger: onHttpPathExists
                    methods: [ DELETE ]
                actions:
                  - action: logger
                    message: path skipped
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/anything")
                .method(HttpMethodEnum.DELETE)
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testTriggerFailsWhenEndpointDoesNotExist() {
        var yaml = """
                alias: no-endpoint
                triggers:
                  - trigger: onHttpPathExists
                    paths: [ "/fail" ]
                    methods: [ GET ]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/fail")
                .method(HttpMethodEnum.GET)
                .endpointExists(false) // <- this blocks the trigger
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testTriggerFailsWithWrongMethod() {
        var yaml = """
                alias: wrong-method
                triggers:
                  - trigger: onHttpPathExists
                    paths: [ "/only-get" ]
                    methods: [ GET ]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/only-get")
                .method(HttpMethodEnum.POST)
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testTriggerFailsWithWrongPath() {
        var yaml = """
                alias: wrong-path
                triggers:
                  - trigger: onHttpPathExists
                    paths: [ "/expected/path" ]
                    methods: [ GET ]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .path("/unexpected")
                .method(HttpMethodEnum.GET)
                .endpointExists(true)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }


}