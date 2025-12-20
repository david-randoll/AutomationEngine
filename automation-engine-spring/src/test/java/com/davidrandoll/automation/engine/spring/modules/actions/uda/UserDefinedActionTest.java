package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
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
                                .params(Map.of("message", "{{ greeting }}, {{ userName }}!"))
                                .build()
                ))
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
                                .build()
                ))
                .actions(List.of(
                        ActionDefinition.builder()
                                .action("logger")
                                .params(Map.of("message", "Action executed during business hours"))
                                .build()
                ))
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

}