package com.automation.engine.http.modules.conditions.http_method;

import ch.qos.logback.classic.Logger;
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
class httpMethodTest {
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
    void testShouldTriggerOnHttpSuccessWithMatchingMethodCondition() {
        var yaml = """
                alias: success-with-method-condition
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    equals: GET
                actions:
                  - action: logger
                    message: triggered due to GET + 2xx
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotTriggerOnHttpSuccessWithWrongMethod() {
        var yaml = """
                alias: success-wrong-method
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    equals: POST
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldNotTriggerWhenHttpMethodIsNull() {
        var yaml = """
                alias: null-method
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    equals: GET
                actions:
                  - action: logger
                    message: method is null
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = HttpRequestEvent.builder()
                .method(null)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void shouldTriggerWhenMethodEquals() {
        var yaml = """
                alias: equals-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    equals: GET
                actions:
                  - action: logger
                    message: method is GET
                """;

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);
    }

    @Test
    void shouldTriggerWhenMethodNotEquals() {
        var yaml = """
                alias: not-equals-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    notEquals: POST
                actions:
                  - action: logger
                    message: method is not POST
                """;

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);
    }

    @Test
    void shouldTriggerWhenMethodInList() {
        var yaml = """
                alias: in-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    in: [GET, POST]
                actions:
                  - action: logger
                    message: method in allowed list
                """;

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.POST)
                .build();

        assertSatisfied(yaml, event);
    }

    @Test
    void shouldTriggerWhenMethodNotInList() {
        var yaml = """
                alias: not-in-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    notIn: [PUT, DELETE]
                actions:
                  - action: logger
                    message: method is not PUT/DELETE
                """;

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);
    }

    @Test
    void shouldTriggerWhenMethodMatchesRegex() {
        var yaml = """
                alias: regex-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    regex: "G.T"
                actions:
                  - action: logger
                    message: method matches regex
                """;

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);
    }

    @Test
    void shouldTriggerWhenMethodMatchesWildcardLike() {
        var yaml = """
                alias: like-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    like: G*
                actions:
                  - action: logger
                    message: method matches wildcard
                """;

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);
    }

    @Test
    void shouldTriggerWhenMethodExistsTrue() {
        var yaml = """
                alias: exists-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    exists: true
                actions:
                  - action: logger
                    message: method exists
                """;

        var event = HttpRequestEvent.builder()
                .method(HttpMethodEnum.GET)
                .build();

        assertSatisfied(yaml, event);
    }

    @Test
    void shouldNotTriggerWhenMethodIsNullButExistsExpected() {
        var yaml = """
                alias: exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpMethod
                    exists: true
                actions:
                  - action: logger
                    message: shouldn't trigger
                """;

        var event = HttpRequestEvent.builder()
                .method(null)
                .build();

        assertNotSatisified(yaml, event);
    }


    private void assertSatisfied(String yaml, HttpRequestEvent event) {
        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
        var context = EventContext.of(event);
        engine.publishEvent(context);
        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    private void assertNotSatisified(String yaml, HttpRequestEvent event) {
        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
        var context = EventContext.of(event);
        engine.publishEvent(context);
        assertThat(automation.allConditionsMet(context)).isFalse();
    }

}