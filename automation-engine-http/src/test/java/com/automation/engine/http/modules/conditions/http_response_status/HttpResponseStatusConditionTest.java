package com.automation.engine.http.modules.conditions.http_response_status;

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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineHttpApplication.class)
@ExtendWith(SpringExtension.class)
class HttpResponseStatusConditionTest {
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
    void testResponseStatusEqualsMatch() {
        var yaml = """
                alias: status-equals-200
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 200
                actions:
                  - action: logger
                    message: should match 200 OK
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusEqualsMismatch() {
        var yaml = """
                alias: status-equals-404
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: 404
                actions:
                  - action: logger
                    message: should not match wrong status
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseStatusNotEqualsMatch() {
        var yaml = """
                alias: status-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notEquals: 500
                actions:
                  - action: logger
                    message: should match if status not 500
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusInMatch() {
        var yaml = """
                alias: status-in-matching
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    in: [200, 201, 204]
                actions:
                  - action: logger
                    message: should match 200 in list
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusInMismatch() {
        var yaml = """
                alias: status-in-not-found
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    in: [301, 302]
                actions:
                  - action: logger
                    message: should not match 404
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.NO_CONTENT).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseStatusNotInMatch() {
        var yaml = """
                alias: status-not-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notIn: [400, 401]
                actions:
                  - action: logger
                    message: should match if status not in list
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusRegexMatch() {
        var yaml = """
                alias: status-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    regex: "^2\\\\d\\\\d$"
                actions:
                  - action: logger
                    message: should match any 2xx status
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.NO_CONTENT).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusExistsTrue() {
        var yaml = """
                alias: status-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: true
                actions:
                  - action: logger
                    message: should match if status exists
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testResponseStatusExistsTrueButMissing() {
        var yaml = """
                alias: status-missing-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: true
                actions:
                  - action: logger
                    message: should not match if status missing
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(null).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testResponseStatusExistsFalseWithNull() {
        var yaml = """
                alias: status-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: false
                actions:
                  - action: logger
                    message: should match if status is missing
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = HttpResponseEvent.builder().responseStatus(null).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchEqualsOK() {
        var yaml = """
                alias: status-equals-ok
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: OK
                actions:
                  - action: logger
                    message: matched OK
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchEqualsOKCaseSensitive() {
        var yaml = """
                alias: status-equals-ok-case-sensitive
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    equals: Ok
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.OK).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchStatusInList() {
        var yaml = """
                alias: status-in-list
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    in: [OK, Created, Accepted]
                actions:
                  - action: logger
                    message: matched in list
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.CREATED).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchNotIn() {
        var yaml = """
                alias: status-not-in-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    notIn: [OK, Created]
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.CREATED)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchRegexNotFound() {
        var yaml = """
                alias: status-regex-not
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    regex: "^Not.*"
                actions:
                  - action: logger
                    message: matched not.*
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(HttpStatus.NOT_FOUND).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchRegexNumericOnOK() {
        var yaml = """
                alias: status-regex-numeric-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    regex: "^2..$"
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.OK)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenStatusExists() {
        var yaml = """
                alias: status-exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: true
                actions:
                  - action: logger
                    message: matched exists
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder()
                .responseStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenStatusDoesNotExist() {
        var yaml = """
                alias: status-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpResponseStatus
                    exists: false
                actions:
                  - action: logger
                    message: matched missing status
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpResponseEvent.builder().responseStatus(null).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }


}