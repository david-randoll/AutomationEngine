package com.davidrandoll.automation.engine.spring.modules.conditions.udc;

import com.davidrandoll.automation.engine.core.Automation;
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

class UserDefinedConditionTest extends AutomationEngineTest {

    @Autowired
    private DefaultUserDefinedConditionRegistry conditionRegistry;

    @BeforeEach
    void setUp() {
        UserDefinedConditionDefinition businessHours = UserDefinedConditionDefinition.builder()
                .name("isBusinessHours")
                .description("Checks if current time is within business hours")
                .parameters(Map.of("startHour", "09:00", "endHour", "17:00"))
                .conditions(List.of(
                        ConditionDefinition.builder()
                                .condition("and")
                                .params(Map.of("conditions", List.of(
                                        Map.of("condition", "time", "after", "{{ startHour }}"),
                                        Map.of("condition", "time", "before", "{{ endHour }}")
                                )))
                                .build()
                ))
                .build();
        conditionRegistry.registerCondition(businessHours);
    }

    @Test
    void testUserDefinedCondition_Met() {
        var yaml = """
                alias: Test User Defined Condition Met
                triggers:
                  - trigger: time
                    at: 10:00
                conditions:
                  - condition: userDefinedCondition
                    name: isBusinessHours
                actions:
                  - action: logger
                    message: "Condition met: Business hours"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Condition met: Business hours"));
    }

    @Test
    void testUserDefinedCondition_NotMet() {
        var yaml = """
                alias: Test User Defined Condition Not Met
                triggers:
                  - trigger: time
                    at: 18:00
                conditions:
                  - condition: userDefinedCondition
                    name: isBusinessHours
                actions:
                  - action: logger
                    message: "This should not log"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(18, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not log"));
    }

    @Test
    void testUserDefinedConditionWithParameters() {
        var yaml = """
                alias: Test User Defined Condition with Parameters
                triggers:
                  - trigger: time
                    at: 08:00
                conditions:
                  - condition: userDefinedCondition
                    name: isBusinessHours
                    startHour: "07:00"
                    endHour: "15:00"
                actions:
                  - action: logger
                    message: "Custom business hours met"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(8, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Custom business hours met"));
    }

    @Test
    void testUserDefinedConditionNotFound() {
        var yaml = """
                alias: Test Non-existent User Defined Condition
                triggers:
                  - trigger: time
                    at: 10:00
                conditions:
                  - condition: userDefinedCondition
                    name: nonExistentCondition
                actions:
                  - action: logger
                    message: "This should not execute"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not execute"));
    }

}