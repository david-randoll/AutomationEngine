package com.davidrandoll.automation.engine.spring.modules.triggers.udt;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserDefinedTriggerTest extends AutomationEngineTest {

    @Autowired
    private DefaultUserDefinedTriggerRegistry triggerRegistry;

    @BeforeEach
    void setUp() {
        UserDefinedTriggerDefinition morningTrigger = UserDefinedTriggerDefinition.builder()
                .name("morningTime")
                .description("Triggers in the morning")
                .parameters(Map.of("time", "09:00"))
                .triggers(List.of(
                        TriggerDefinition.builder()
                                .trigger("time")
                                .params(Map.of("at", "{{ time }}"))
                                .build()
                ))
                .build();
        triggerRegistry.registerTrigger(morningTrigger);

        UserDefinedTriggerDefinition breakTimes = UserDefinedTriggerDefinition.builder()
                .name("breakTimes")
                .description("Triggers during break times")
                .triggers(List.of(
                        TriggerDefinition.builder()
                                .trigger("time")
                                .params(Map.of("at", "10:30"))
                                .build(),
                        TriggerDefinition.builder()
                                .trigger("time")
                                .params(Map.of("at", "15:30"))
                                .build()
                ))
                .build();
        triggerRegistry.registerTrigger(breakTimes);
    }

    @Test
    void testUserDefinedTrigger_Triggers() {
        var yaml = """
                alias: Test User Defined Trigger
                triggers:
                  - trigger: userDefinedTrigger
                    name: morningTime
                actions:
                  - action: logger
                    message: "Morning trigger activated"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(9, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Morning trigger activated"));
    }

    @Test
    void testUserDefinedTrigger_DoesNotTrigger() {
        var yaml = """
                alias: Test User Defined Trigger Not Triggered
                triggers:
                  - trigger: userDefinedTrigger
                    name: morningTime
                actions:
                  - action: logger
                    message: "This should not log"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not log"));
    }

    @Test
    void testUserDefinedTriggerWithCustomParameter() {
        var yaml = """
                alias: Test User Defined Trigger with Custom Parameter
                triggers:
                  - trigger: userDefinedTrigger
                    name: morningTime
                    time: "08:00"
                actions:
                  - action: logger
                    message: "Custom morning time trigger"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(8, 0));
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Custom morning time trigger"));
    }

    @Test
    void testUserDefinedTriggerWithMultipleTimes() {
        var yaml = """
                alias: Test User Defined Trigger with Multiple Times
                triggers:
                  - trigger: userDefinedTrigger
                    name: breakTimes
                actions:
                  - action: logger
                    message: "Break time!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event1 = new TimeBasedEvent(LocalTime.of(10, 30));
        engine.publishEvent(event1);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Break time!"));

        logAppender.clear();

        TimeBasedEvent event2 = new TimeBasedEvent(LocalTime.of(15, 30));
        engine.publishEvent(event2);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Break time!"));
    }

    @Test
    void testUserDefinedTriggerNotFound() {
        var yaml = """
                alias: Test Non-existent User Defined Trigger
                triggers:
                  - trigger: userDefinedTrigger
                    name: nonExistentTrigger
                    throwErrorIfNotFound: false
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