package com.automation.engine.http.modules.triggers.on_http_response;

import ch.qos.logback.classic.Logger;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.creator.AutomationCreator;
import com.automation.engine.http.AutomationEngineHttpApplication;
import com.automation.engine.http.JsonTestUtils;
import com.automation.engine.http.TestLogAppender;
import com.automation.engine.http.event.HttpResponseEvent;
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
class OnHttpServerErrorResponseTriggerTest {
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
    void testShouldTriggerOnServerErrorAndMatchStatus() {
        var yaml = """
                alias: server error 500
                triggers:
                  - trigger: onHttpServerErrorResponse
                    responseStatus: 500
                actions:
                  - action: logger
                    message: server error triggered
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerOnClientError() {
        var yaml = """
                alias: should not trigger on 404
                triggers:
                  - trigger: onHttpServerErrorResponse
                    responseStatus: 500
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.NOT_FOUND)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldNotTriggerOnSuccess() {
        var yaml = """
                alias: should not trigger on 200
                triggers:
                  - trigger: onHttpServerErrorResponse
                    responseStatus: 500
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
    void testShouldNotTriggerIfInnerConditionFails() {
        var yaml = """
                alias: should not match inner
                triggers:
                  - trigger: onHttpServerErrorResponse
                    responseBody:
                      message: "something went wrong"
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = JsonTestUtils.json("""
                {
                  "message": "other error"
                }
                """);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .responseBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldTriggerOn503WithMatchingBody() {
        var yaml = """
                alias: 503 with body
                triggers:
                  - trigger: onHttpServerErrorResponse
                    responseBody:
                      message: "service unavailable"
                actions:
                  - action: logger
                    message: matched 503
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = JsonTestUtils.json("""
                {
                  "message": "service unavailable"
                }
                """);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .responseBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerIfStatusMissing() {
        var yaml = """
                alias: missing status test
                triggers:
                  - trigger: onHttpServerErrorResponse
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
                .build(); // no status set

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }


}