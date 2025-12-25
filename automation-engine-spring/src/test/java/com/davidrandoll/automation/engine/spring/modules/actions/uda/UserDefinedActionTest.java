package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.creator.result.ResultDefinition;
import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserDefinedActionTest extends AutomationEngineTest {

    @Autowired
    private DefaultUserDefinedActionRegistry actionRegistry;

    @BeforeEach
    void setUp() {
        // Register a simple user-defined action
        UserDefinedActionDefinition simpleAction = UserDefinedActionDefinition.builder()
                .parameters(Map.of("userName", "Guest", "greeting", "Hello"))
                .name("greetUser")
                .description("Greets a user with a message")
                .actions(List.of(
                        ActionDefinition.builder()
                                .action("logger")
                                .params(Map.of("message",
                                        "{{ greeting }}, {{ userName }}!"))
                                .build()))
                .build();
        actionRegistry.registerAction(simpleAction);
    }

    @Test
    void testSimpleUserDefinedAction() {
        var yaml = """
                alias: Test Simple User Defined Action
                triggers:
                  - trigger: time
                    at: 10:00
                actions:
                  - action: userDefinedAction
                    name: greetUser
                    userName: "John Doe"
                    greeting: "Welcome"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Welcome, John Doe!"));
    }

    @Test
    void testUserDefinedActionWithDefaultParameters() {
        var yaml = """
                alias: Test User Defined Action with Default Parameters
                triggers:
                  - trigger: time
                    at: 11:00
                actions:
                  - action: userDefinedAction
                    name: greetUser
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(11, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Hello, Guest!"));
    }

    @Test
    void testUserDefinedActionWithConditions_Met() {
        UserDefinedActionDefinition conditionalAction = UserDefinedActionDefinition.builder()
                .name("businessHoursAction")
                .description("Only executes during business hours")
                .conditions(List.of(
                        ConditionDefinition.builder()
                                .condition("time")
                                .params(Map.of("after", "09:00", "before", "17:00"))
                                .build()))
                .actions(List.of(
                        ActionDefinition.builder()
                                .action("logger")
                                .params(Map.of("message",
                                        "Action executed during business hours"))
                                .build()))
                .build();
        actionRegistry.registerAction(conditionalAction);

        var yaml = """
                alias: Test User Defined Action with Conditions Met
                triggers:
                  - trigger: time
                    at: 12:00
                actions:
                  - action: userDefinedAction
                    name: businessHoursAction
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(12, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Action executed during business hours"));
    }

    @Test
    void testUserDefinedActionNotFound() {
        var yaml = """
                alias: Test Non-existent User Defined Action
                triggers:
                  - trigger: time
                    at: 10:00
                actions:
                  - action: userDefinedAction
                    name: nonExistentAction
                    throwErrorIfNotFound: false
                  - action: logger
                    message: "After non-existent action"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("After non-existent action"));
    }

    @Test
    void testUserDefinedActionWithResult_SimpleValue() {
        UserDefinedActionDefinition actionWithResult = UserDefinedActionDefinition.builder()
                .name("returnValue")
                .result(ResultDefinition.builder()
                        .result("basic")
                        .params(objectMapper.valueToTree("Hello World"))
                        .build())
                .build();
        actionRegistry.registerAction(actionWithResult);

        var yaml = """
                alias: Test User Defined Action with Simple Result
                triggers:
                  - trigger: time
                    at: 14:00
                actions:
                  - action: userDefinedAction
                    name: returnValue
                    storeToVariable: myResult
                  - action: logger
                    message: "The result is {{ myResult }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(14, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("The result is Hello World"));
    }

    @Test
    void testUserDefinedActionWithResult_Expression() {
        UserDefinedActionDefinition actionWithResult = UserDefinedActionDefinition.builder()
                .name("calculateSum")
                .parameters(Map.of("a", 0, "b", 0))
                .result(ResultDefinition.builder()
                        .result("basic")
                        .params(objectMapper.valueToTree("{{ a + b }}"))
                        .build())
                .build();
        actionRegistry.registerAction(actionWithResult);

        var yaml = """
                alias: Test User Defined Action with Expression Result
                triggers:
                  - trigger: time
                    at: 15:00
                actions:
                  - action: userDefinedAction
                    name: calculateSum
                    a: 10
                    b: 20
                    storeToVariable: myResult
                  - action: logger
                    message: "The sum is {{ myResult }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(15, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("The sum is 30"));
    }

    @Test
    void testUserDefinedActionWithResult_ComplexObject() {
        UserDefinedActionDefinition actionWithResult = UserDefinedActionDefinition.builder()
                .name("getUserInfo")
                .result(ResultDefinition.builder()
                        .result("basic")
                        .params(objectMapper.valueToTree(Map.of(
                                "id", 123,
                                "name", "{{ userName }}",
                                "role", "admin")))
                        .build())
                .build();
        actionRegistry.registerAction(actionWithResult);

        var yaml = """
                alias: Test User Defined Action with Complex Result
                triggers:
                  - trigger: time
                    at: 16:00
                actions:
                  - action: userDefinedAction
                    name: getUserInfo
                    userName: "Alice"
                    storeToVariable: userInfo
                  - action: logger
                    message: "User {{ userInfo.name }} has ID {{ userInfo.id }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(16, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("User Alice has ID 123"));
    }

    @Test
    void testUserDefinedActionWithVariablesAndResult() {
        UserDefinedActionDefinition actionWithVars = UserDefinedActionDefinition.builder()
                .name("calculateWithVars")
                .variables(List.of(
                        VariableDefinition.builder()
                                .variable("basic")
                                .params(Map.of("multiplier", 2))
                                .build()))
                .parameters(Map.of("base", 0))
                .result(ResultDefinition.builder()
                        .result("basic")
                        .params(objectMapper.valueToTree("{{ base * multiplier }}"))
                        .build())
                .build();
        actionRegistry.registerAction(actionWithVars);

        var yaml = """
                alias: Test User Defined Action with Variables and Result
                triggers:
                  - trigger: time
                    at: 17:00
                actions:
                  - action: userDefinedAction
                    name: calculateWithVars
                    base: 50
                    storeToVariable: finalResult
                  - action: logger
                    message: "The final result is {{ finalResult }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(17, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("The final result is 100"));
    }
}