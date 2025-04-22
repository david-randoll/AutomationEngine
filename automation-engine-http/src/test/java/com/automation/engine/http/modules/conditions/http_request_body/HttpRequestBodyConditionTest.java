package com.automation.engine.http.modules.conditions.http_request_body;

import ch.qos.logback.classic.Logger;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.creator.AutomationCreator;
import com.automation.engine.http.AutomationEngineHttpApplication;
import com.automation.engine.http.JsonTestUtils;
import com.automation.engine.http.TestLogAppender;
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
class HttpRequestBodyConditionTest {
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
    void testRequestBodyRootFieldEquals() {
        var yaml = """
                alias: body-root-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      name:
                        equals: David
                actions:
                  - action: logger
                    message: body root matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "name": "David"
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyNestedFieldMatch() {
        var yaml = """
                alias: nested-body-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      person:
                        age:
                          equals: "30"
                actions:
                  - action: logger
                    message: nested body match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "person": {
                    "age": 30
                  }
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyTypeMismatchShouldMatch() {
        var yaml = """
                alias: body-type-mismatch
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      active:
                        equals: true
                actions:
                  - action: logger
                    message: shouldn't match because type mismatch
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "active": "true"
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyRegexMatch() {
        var yaml = """
                alias: body-regex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      username:
                        regex: "user[0-9]+"
                actions:
                  - action: logger
                    message: regex matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "username": "user123"
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyFieldMissingShouldNotMatch() {
        var yaml = """
                alias: body-field-missing
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      id:
                        exists: true
                actions:
                  - action: logger
                    message: should not trigger
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testRequestBodyArrayContainsValue() {
        var yaml = """
                alias: body-array-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      tags:
                        in: [ "spring", "java", "automation" ]
                actions:
                  - action: logger
                    message: array contains value
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "tags": [ "java", "devops" ]
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyFieldNotInList() {
        var yaml = """
                alias: body-not-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      status:
                        notIn: [ "failed", "error" ]
                actions:
                  - action: logger
                    message: status not in [failed, error]
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "status": "success"
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyFieldLike() {
        var yaml = """
                alias: body-like-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      description:
                        like: "*success*"
                actions:
                  - action: logger
                    message: like pattern matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "description": "operation success confirmed"
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyFieldNotEquals() {
        var yaml = """
                alias: body-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      type:
                        notEquals: admin
                actions:
                  - action: logger
                    message: not admin
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "type": "user"
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyMultipleFieldsMatch() {
        var yaml = """
                alias: multiple-body-fields
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      user:
                        equals: david
                      verified:
                        equals: true
                actions:
                  - action: logger
                    message: multiple matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "user": "david",
                  "verified": true
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyFieldIsNullShouldNotMatchExistsTrue() {
        var yaml = """
                alias: body-null-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      country:
                        exists: true
                actions:
                  - action: logger
                    message: should not match if country is null
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "country": null
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testRequestBodyLikePatternMatch() {
        var yaml = """
                alias: body-like-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      description:
                        like: "%important%"
                actions:
                  - action: logger
                    message: description contains important
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "description": "This is an important update."
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyNotEquals() {
        var yaml = """
                alias: body-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      level:
                        notEquals: debug
                actions:
                  - action: logger
                    message: level is not debug
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "level": "info"
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyMultipleFieldMatch() {
        var yaml = """
                alias: body-multi-field
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      role:
                        equals: admin
                      active:
                        equals: true
                actions:
                  - action: logger
                    message: role is admin and active
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "role": "admin",
                  "active": true
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyFieldIsNull() {
        var yaml = """
                alias: body-null
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      userId:
                        exists: false
                actions:
                  - action: logger
                    message: userId not present
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testRequestBodyArrayOfObjectsDoesNotMatch() {
        var yaml = """
                alias: body-array-objects
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      items:
                        equals: "value"
                actions:
                  - action: logger
                    message: should not match array of objects
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var requestBody = JsonTestUtils.json("""
                {
                  "items": [
                    { "name": "value" }
                  ]
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse(); // Should not match
    }

    @Test
    void testNestedRequestBodyFieldEquals() {
        var yaml = """
                alias: body-nested-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      user:
                        profile:
                          name:
                            equals: David
                actions:
                  - action: logger
                    message: matched nested name
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var requestBody = JsonTestUtils.json("""
                {
                  "user": {
                    "profile": {
                      "name": "David"
                    }
                  }
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testNestedRequestBodyFieldMismatch() {
        var yaml = """
                alias: body-nested-not-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      user.profile.name:
                        equals: Ishwari
                actions:
                  - action: logger
                    message: should not match wrong name
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var requestBody = JsonTestUtils.json("""
                {
                  "user": {
                    "profile": {
                      "name": "David"
                    }
                  }
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testNestedRequestBodyFieldInList() {
        var yaml = """
                alias: body-nested-in
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      user:
                        profile:
                          role:
                            in: [admin, manager]
                actions:
                  - action: logger
                    message: role in list
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var requestBody = JsonTestUtils.json("""
                {
                  "user": {
                    "profile": {
                      "role": "manager"
                    }
                  }
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testNestedFieldMissingWithExistsFalse() {
        var yaml = """
                alias: body-nested-missing
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      user:
                        profile:
                          age:
                            exists: false
                actions:
                  - action: logger
                    message: age not present
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var requestBody = JsonTestUtils.json("""
                {
                  "user": {
                    "profile": {
                      "name": "David"
                    }
                  }
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testNestedFieldIsExplicitNull() {
        var yaml = """
                alias: body-nested-null
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      user.profile.phone:
                        exists: false
                actions:
                  - action: logger
                    message: phone is null or missing
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var requestBody = JsonTestUtils.json("""
                {
                  "user": {
                    "profile": {
                      "phone": null
                    }
                  }
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        // Assuming `null` still counts as not existing
        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testNestedPathMidpointNotObject() {
        var yaml = """
                alias: body-nested-invalid-path
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpRequestBody
                    requestBody:
                      user.profile.name.first:
                        equals: David
                actions:
                  - action: logger
                    message: should not match invalid nested path
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var requestBody = JsonTestUtils.json("""
                {
                  "user": {
                    "profile": {
                      "name": "David"
                    }
                  }
                }
                """);

        var event = HttpRequestEvent.builder().requestBody(requestBody).build();
        var context = EventContext.of(event);
        engine.publishEvent(context);

        // profile.name is a string, so accessing .first should fail
        assertThat(automation.allConditionsMet(context)).isFalse();
    }
}