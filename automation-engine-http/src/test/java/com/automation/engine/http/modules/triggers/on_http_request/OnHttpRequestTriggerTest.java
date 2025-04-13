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


}