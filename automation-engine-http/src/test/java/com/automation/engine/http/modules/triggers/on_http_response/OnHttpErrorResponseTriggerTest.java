package com.automation.engine.http.modules.triggers.on_http_response;

import ch.qos.logback.classic.Logger;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.creator.AutomationCreator;
import com.automation.engine.http.AutomationEngineHttpApplication;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineHttpApplication.class)
@ExtendWith(SpringExtension.class)
class OnHttpErrorResponseTriggerTest {
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
    void testShouldTriggerOnErrorResponseAndMatch() {
        var yaml = """
                alias: Error response trigger match
                triggers:
                  - trigger: onHttpErrorResponse
                    errorDetail:
                      message: "Internal error"
                actions:
                  - action: logger
                    message: should trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> errorDetail = Map.of("message", "Internal error");
        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorDetail(errorDetail)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerOnSuccessResponse() {
        var yaml = """
                alias: Error trigger should not activate on 200
                triggers:
                  - trigger: onHttpErrorResponse
                    errorDetail:
                      message: "Something wrong"
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> errorDetail = Map.of("message", "Something wrong");
        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK) // 200 - success
                .errorDetail(errorDetail)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldNotTriggerOnErrorResponseWithNonMatchingErrorDetail() {
        var yaml = """
                alias: Error response with wrong detail
                triggers:
                  - trigger: onHttpErrorResponse
                    errorDetail:
                      message: "Access Denied"
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        Map<String, Object> errorDetail = Map.of("message", "Internal Server Error"); // different
        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.BAD_REQUEST)
                .errorDetail(errorDetail)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }

    @Test
    void testShouldTriggerFor500ErrorAndMatchingCondition() {
        var yaml = """
                alias: 500 error match
                triggers:
                  - trigger: onHttpErrorResponse
                    responseStatus: 500
                actions:
                  - action: logger
                    message: should trigger
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
    void testShouldNotTriggerIfNoStatusPresent() {
        var yaml = """
                alias: Missing status
                triggers:
                  - trigger: onHttpErrorResponse
                    responseStatus: 500
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().build(); // no status

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
    }


}