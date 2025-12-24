package com.davidrandoll.automation.engine.spring.modules.variables.udv;

import com.davidrandoll.automation.engine.core.Automation;
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

class UserDefinedVariableTest extends AutomationEngineTest {

    @Autowired
    private DefaultUserDefinedVariableRegistry variableRegistry;

    @BeforeEach
    void setUp() {
        UserDefinedVariableDefinition appConfig = UserDefinedVariableDefinition.builder()
                .name("loadAppConfig")
                .description("Loads application configuration")
                .parameters(Map.of("environment", "production"))
                .variables(List.of(
                        VariableDefinition.builder()
                                .variable("basic")
                                .params(Map.of(
                                        "appName", "AutomationEngine",
                                        "version", "1.0",
                                        "env", "{{ environment }}"
                                ))
                                .build()
                ))
                .build();
        variableRegistry.registerVariable(appConfig);

        UserDefinedVariableDefinition timeVars = UserDefinedVariableDefinition.builder()
                .name("loadTimeVariables")
                .description("Loads formatted time variables")
                .variables(List.of(
                        VariableDefinition.builder()
                                .variable("basic")
                                .params(Map.of(
                                        "formattedTime", "{{ time | time_format(pattern='hh:mm a') }}",
                                        "hour", "{{ time | time_format(pattern='HH') }}"
                                ))
                                .build()
                ))
                .build();
        variableRegistry.registerVariable(timeVars);
    }

    @Test
    void testSimpleUserDefinedVariable() {
        var yaml = """
                alias: Test Simple User Defined Variable
                triggers:
                  - trigger: time
                    at: 10:00
                variables:
                  - variable: userDefinedVariable
                    name: loadAppConfig
                actions:
                  - action: logger
                    message: "App: {{ appName }} v{{ version }} ({{ env }})"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("App: AutomationEngine v1.0 (production)"));
    }

    @Test
    void testUserDefinedVariableWithCustomParameter() {
        var yaml = """
                alias: Test User Defined Variable with Custom Parameter
                triggers:
                  - trigger: time
                    at: 10:00
                variables:
                  - variable: userDefinedVariable
                    name: loadAppConfig
                    environment: "development"
                actions:
                  - action: logger
                    message: "Environment: {{ env }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Environment: development"));
    }

    @Test
    void testUserDefinedVariableWithTimeFormatting() {
        var yaml = """
                alias: Test User Defined Variable with Time Formatting
                triggers:
                  - trigger: time
                    at: 14:30
                variables:
                  - variable: userDefinedVariable
                    name: loadTimeVariables
                actions:
                  - action: logger
                    message: "Current time: {{ formattedTime }}, Hour: {{ hour }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(14, 30));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Current time: 02:30 PM, Hour: 14"));
    }

    @Test
    void testUserDefinedVariableNotFound() {
        var yaml = """
                alias: Test Non-existent User Defined Variable
                triggers:
                  - trigger: time
                    at: 10:00
                variables:
                  - variable: userDefinedVariable
                    name: nonExistentVariable
                    throwErrorIfNotFound: false
                actions:
                  - action: logger
                    message: "Should still execute"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Should still execute"));
    }

    @Test
    void testMultipleUserDefinedVariables() {
        UserDefinedVariableDefinition userProfile = UserDefinedVariableDefinition.builder()
                .name("loadUserProfile")
                .description("Loads user profile information")
                .parameters(Map.of("userId", "guest"))
                .variables(List.of(
                        VariableDefinition.builder()
                                .variable("basic")
                                .params(Map.of(
                                        "userName", "User {{ userId }}",
                                        "displayName", "Welcome, User {{ userId }}!"
                                ))
                                .build()
                ))
                .build();
        variableRegistry.registerVariable(userProfile);

        var yaml = """
                alias: Test Multiple User Defined Variables
                triggers:
                  - trigger: time
                    at: 10:00
                variables:
                  - variable: userDefinedVariable
                    name: loadAppConfig
                  - variable: userDefinedVariable
                    name: loadUserProfile
                    userId: "alice"
                actions:
                  - action: logger
                    message: "{{ appName }} - {{ displayName }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("AutomationEngine - Welcome, User alice!"));
    }

}