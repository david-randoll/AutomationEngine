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
class OnHttpSuccessResponseTriggerTest {
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
    void testShouldTriggerOnSuccess200() {
        var yaml = """
                alias: success 200 trigger
                triggers:
                  - trigger: onHttpSuccessResponse
                    responseStatus: 200
                actions:
                  - action: logger
                    message: success match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK)
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
                  - trigger: onHttpSuccessResponse
                    responseStatus: 200
                actions:
                  - action: logger
                    message: should not match
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
    void testShouldNotTriggerOnServerError() {
        var yaml = """
                alias: server error not success
                triggers:
                  - trigger: onHttpSuccessResponse
                    responseStatus: 200
                actions:
                  - action: logger
                    message: wrong status
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
    void testShouldNotTriggerIfInnerDoesNotMatch() {
        var yaml = """
                alias: wrong body in success
                triggers:
                  - trigger: onHttpSuccessResponse
                    responseBody:
                      message: "hello"
                actions:
                  - action: logger
                    message: should not activate
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var body = JsonTestUtils.json("""
                {
                  "message": "bye"
                }
                """);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK)
                .responseBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldTriggerOn201WithMatchingBody() {
        var yaml = """
                alias: 201 with body
                triggers:
                  - trigger: onHttpSuccessResponse
                    responseStatus: 201
                    responseBody:
                      id: "123"
                      status: "created"
                actions:
                  - action: logger
                    message: 201 matched
                """;

        var body = JsonTestUtils.json("""
                {
                  "id": "123",
                  "status": "created"
                }
                """);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.CREATED)
                .responseBody(body)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerIfStatusMissing() {
        var yaml = """
                alias: no status in event
                triggers:
                  - trigger: onHttpSuccessResponse
                    responseBody:
                      result: "ok"
                actions:
                  - action: logger
                    message: should not match
                """;

        var body = JsonTestUtils.json("""
                {
                  "result": "ok"
                }
                """);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseBody(body)
                .build(); // missing status

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }


}