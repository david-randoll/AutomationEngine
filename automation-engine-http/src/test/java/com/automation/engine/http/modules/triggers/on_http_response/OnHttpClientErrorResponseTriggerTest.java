package com.automation.engine.http.modules.triggers.on_http_response;

import ch.qos.logback.classic.Logger;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.creator.AutomationCreator;
import com.automation.engine.http.AutomationEngineHttpApplication;
import com.automation.engine.http.JsonTestUtils;
import com.automation.engine.http.TestLogAppender;
import com.automation.engine.http.event.HttpResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineHttpApplication.class)
@ExtendWith(SpringExtension.class)
class OnHttpClientErrorResponseTriggerTest {
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
    void testShouldTriggerOnClientErrorResponseAndMatch() {
        var yaml = """
                alias: Client error trigger match
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseStatus: 400
                actions:
                  - action: logger
                    message: client error triggered
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.BAD_REQUEST)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerOnSuccessResponse() {
        var yaml = """
                alias: Should not trigger on 200
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseStatus: 400
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldNotTriggerOnServerErrorResponse() {
        var yaml = """
                alias: Should not trigger on 500
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseStatus: 400
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldNotTriggerIfConditionFails() {
        var yaml = """
                alias: Client error condition fails
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseBody:
                      message: "Unauthorized"
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = JsonTestUtils.json("""
                {
                  "message": "Different error"
                }
                """);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.UNAUTHORIZED) // 401
                .responseBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldTriggerOnClientErrorWithBodyMatch() {
        var yaml = """
                alias: 403 forbidden with message
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseBody:
                      error: "Forbidden"
                actions:
                  - action: logger
                    message: triggered
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = JsonTestUtils.json("""
                {
                  "error": "Forbidden"
                }
                """);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.FORBIDDEN) // 403
                .responseBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerIfStatusMissing() {
        var yaml = """
                alias: Missing status
                triggers:
                  - trigger: onHttpClientErrorResponse
                    responseBody:
                      message: "error"
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = JsonTestUtils.json("""
                {
                  "message": "error"
                }
                """);

        var event = HttpResponseEvent.builder()
                .responseBody(body)
                .build(); // no status

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }
}