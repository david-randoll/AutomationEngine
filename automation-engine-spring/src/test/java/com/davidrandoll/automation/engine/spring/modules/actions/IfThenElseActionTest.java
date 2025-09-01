package com.davidrandoll.automation.engine.spring.modules.actions;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class IfThenElseActionTest extends AutomationEngineTest {

    @Test
    void testIfConditionMetThenActionExecuted() {
        var yaml = """
                alias: If Then Action Test
                triggers:
                  - trigger: time
                    at: 22:37
                conditions:
                  - condition: time
                    after: 22:30
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        after: 22:30
                    then:
                      - action: logger
                        message: "Condition satisfied, executed THEN actions."
                    else:
                      - action: logger
                        message: "Executed ELSE actions."
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create an event where the condition is met
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37)); // Condition: after 22:30

        // Process event
        engine.publishEvent(event);

        // Assert: THEN action should be executed
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Condition satisfied, executed THEN actions."))
                .noneMatch(msg -> msg.contains("Executed ELSE actions."));
    }

    @Test
    void testIfConditionNotMetElseActionExecuted() {
        var yaml = """
                alias: If Then Else Action Test
                triggers:
                  - trigger: time
                    at: 22:37
                conditions:
                  - condition: time
                    after: 22:30
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        after: 23:00
                    then:
                      - action: logger
                        message: "Condition satisfied, executed THEN actions."
                    else:
                      - action: logger
                        message: "Executed ELSE actions."
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create an event where the condition is not met
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37)); // Condition: after 23:00 is not satisfied

        // Process event
        engine.publishEvent(event);

        // Assert: ELSE action should be executed
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Executed ELSE actions."))
                .noneMatch(msg -> msg.contains("Condition satisfied, executed THEN actions."));
    }

    @Test
    void testMultipleIfThenBlocksWithFirstConditionMet() {
        var yaml = """
                alias: Multiple If Then Blocks Test
                triggers:
                  - trigger: time
                    at: 22:37
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        after: 22:30
                    then:
                      - action: logger
                        message: "Condition satisfied in the first block, executed THEN actions."
                    ifs:
                      - if:
                          - condition: time
                            after: 23:00
                        then:
                          - action: logger
                            message: "This block won't execute."
                    else:
                      - action: logger
                        message: "Executed ELSE actions."
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create an event where the first condition is met
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37)); // Condition: after 22:30

        // Process event
        engine.publishEvent(event);

        // Assert: First block's THEN action should be executed, others should not
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Condition satisfied in the first block, executed THEN actions."))
                .noneMatch(msg -> msg.contains("This block won't execute."))
                .noneMatch(msg -> msg.contains("Executed ELSE actions."));
    }

    @Test
    void testMultipleIfThenBlocksWithNoConditionMet() {
        var yaml = """
                alias: Multiple If Then Blocks No Condition Test
                triggers:
                  - trigger: time
                    at: 22:37
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        after: 23:00
                    then:
                      - action: logger
                        message: "This block won't execute."
                    ifs:
                      - if:
                          - condition: time
                            after: 23:30
                        then:
                          - action: logger
                            message: "This block won't execute either."
                    else:
                      - action: logger
                        message: "Executed ELSE actions."
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create an event where none of the conditions are met
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37)); // No condition is satisfied

        // Process event
        engine.publishEvent(event);

        // Assert: ELSE action should be executed
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Executed ELSE actions."))
                .noneMatch(msg -> msg.contains("This block won't execute."));
    }

    @Test
    void testNestedIfThenElses() {
        var yaml = """
                alias: Nested If Then Else Action Test
                triggers:
                  - trigger: time
                    at: 22:37
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        after: 22:30
                    then:
                      - action: ifThenElse
                        if:
                          - condition: time
                            before: 23:00
                        then:
                          - action: logger
                            message: "Nested condition met, executed nested THEN actions."
                        else:
                          - action: logger
                            message: "Nested condition failed, executed nested ELSE actions."
                    else:
                      - action: logger
                        message: "Main condition failed, executed main ELSE actions."
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create an event where the main condition and nested condition are both met
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37)); // Condition: after 22:30 and before 23:00

        // Process event
        engine.publishEvent(event);

        // Assert: Nested THEN action should be executed
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Nested condition met, executed nested THEN actions."))
                .noneMatch(msg -> msg.contains("Main condition failed, executed main ELSE actions."))
                .noneMatch(msg -> msg.contains("Nested condition failed, executed nested ELSE actions."));
    }

    @Test
    void testIfThenElseWithMultipleIfThenBlocks_Satisfied() {
        var yaml = """
                alias: If-Then-Else with Multiple If-Then Blocks Test
                triggers:
                  - trigger: time
                    at: 22:15
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        after: 22:00
                    then:
                      - action: logger
                        message: Time is after 10 PM
                    ifs:
                      - if:
                          - condition: time
                            before: 22:30
                        then:
                          - action: logger
                            message: Time is before 10:30 PM
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - action: logger
                            message: Time is after 10:30 PM
                    else:
                      - action: logger
                        message: Else condition triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events that satisfy the second if condition (before 10:30 PM)
        TimeBasedEvent eventBefore1030 = new TimeBasedEvent(LocalTime.of(22, 15));

        // Process event
        engine.publishEvent(eventBefore1030);

        // Assert: Should trigger the first if-then block and the second if-then block
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Time is after 10 PM"))
                .noneMatch(msg -> msg.contains("Time is before 10:30 PM"))
                .noneMatch(msg -> msg.contains("Time is after 10:30 PM"))
                .noneMatch(msg -> msg.contains("Else condition triggered"));
    }

    @Test
    void testIfThenElseWithMultipleIfThenBlocks_Unsatisfied() {
        var yaml = """
                alias: If-Then-Else with Multiple If-Then Blocks Unsatisfied Test
                triggers:
                  - trigger: time
                    at: 09:45
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        after: 22:00
                    then:
                      - action: logger
                        message: Time is after 10 PM
                    ifs:
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - action: logger
                            message: Time is before 10:30 PM
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - action: logger
                            message: Time is after 10:30 PM
                    else:
                      - action: logger
                        message: Else condition triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that doesn't match any "if" condition (after 10:45 PM)
        TimeBasedEvent eventAfter1045 = new TimeBasedEvent(LocalTime.of(9, 45));

        // Process event
        engine.publishEvent(eventAfter1045);

        // Assert: Should trigger the "else" block
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Else condition triggered"))
                .noneMatch(msg -> msg.contains("Time is after 10 PM"))
                .noneMatch(msg -> msg.contains("Time is before 10:30 PM"))
                .noneMatch(msg -> msg.contains("Time is after 10:30 PM"));
    }

    @Test
    void testIfThenElseWithAllConditionsInIfBlocksFail() {
        var yaml = """
                alias: If-Then-Else with All If Conditions Failing Test
                triggers:
                  - trigger: time
                    at: 09:30
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        after: 22:00
                    then:
                      - action: logger
                        message: Time is after 10 PM
                    ifs:
                      - if:
                          - condition: time
                            after: 22:00
                        then:
                          - action: logger
                            message: Time is after 10:00 PM
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - action: logger
                            message: Time is after 10:30 PM
                    else:
                      - action: logger
                        message: Else condition triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that fails all "if" conditions (before 10:00 PM)
        TimeBasedEvent eventBefore10 = new TimeBasedEvent(LocalTime.of(9, 30));

        // Process event
        engine.publishEvent(eventBefore10);

        // Assert: Should trigger the "else" block
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Else condition triggered"))
                .noneMatch(msg -> msg.contains("Time is after 10 PM"))
                .noneMatch(msg -> msg.contains("Time is before 10:00 PM"))
                .noneMatch(msg -> msg.contains("Time is after 10:30 PM"));
    }

    @Test
    void testIfThenElseWithFirstIfBlockSatisfied_SecondBlockFails() {
        var yaml = """
                alias: If-Then-Else with First If Block Satisfied Test
                triggers:
                  - trigger: time
                    at: 22:35
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        after: 22:00
                    then:
                      - action: logger
                        message: Time is after 10 PM
                    ifs:
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - action: logger
                            message: Time is after 10:30 PM
                      - if:
                          - condition: time
                            before: 22:00
                        then:
                          - action: logger
                            message: Time is before 10 PM
                    else:
                      - action: logger
                        message: Else condition triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that satisfies the first if block condition (after 10:30 PM)
        TimeBasedEvent eventAfter1030 = new TimeBasedEvent(LocalTime.of(22, 35));

        // Process event
        engine.publishEvent(eventAfter1030);

        // Assert: Should trigger the "else" block because the second "if" block fails
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Time is after 10 PM"))
                .noneMatch(msg -> msg.contains("Time is after 10:30 PM"))
                .noneMatch(msg -> msg.contains("Time is before 10 PM"))
                .noneMatch(msg -> msg.contains("Else condition triggered"));
    }

    @Test
    void testIfThenElseWithElseBlockTrigger() {
        var yaml = """
                alias: If-Then-Else with Else Block Test
                triggers:
                  - trigger: time
                    at: 21:30
                actions:
                  - action: ifThenElse
                    ifs:
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - action: logger
                            message: Time is after 10:30 PM
                    else:
                      - action: logger
                        message: Else condition triggered
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that fails all if conditions (before 10 PM)
        TimeBasedEvent eventBefore10PM = new TimeBasedEvent(LocalTime.of(21, 30));

        // Process event
        engine.publishEvent(eventBefore10PM);

        // Assert: Should trigger the "else" block
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Else condition triggered"))
                .noneMatch(msg -> msg.contains("Time is after 10:30 PM"));
    }

    @Test
    void testIfThenElseWithMultipleIfThenBlocks_FirstBlockSatisfied() {
        var yaml = """
                alias: If-Then-Else with Multiple If-Then Blocks, First Block Satisfied Test
                triggers:
                  - trigger: time
                    at: 22:15
                actions:
                  - action: ifThenElse
                    ifs:
                      - if:
                          - condition: time
                            after: 22:00
                        then:
                          - action: logger
                            message: Time is after 10 PM
                      - if:
                          - condition: time
                            after: 23:00
                        then:
                          - action: logger
                            message: Time is after 11 PM
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that satisfies the first "if" block condition (after 10 PM)
        TimeBasedEvent eventAfter10PM = new TimeBasedEvent(LocalTime.of(22, 15));

        // Process event
        engine.publishEvent(eventAfter10PM);

        // Assert: Should trigger the first "if" block
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Time is after 10 PM"))
                .noneMatch(msg -> msg.contains("Time is after 11 PM"));
    }

    @Test
    void testIfThenElseWithMultipleIfThenBlocks_SecondBlockSatisfied() {
        var yaml = """
                alias: If-Then-Else with Multiple If-Then Blocks, Second Block Satisfied Test
                triggers:
                  - trigger: time
                    at: 22:30
                actions:
                  - action: ifThenElse
                    ifs:
                      - if:
                          - condition: time
                            before: 22:00
                        then:
                          - action: logger
                            message: Time is before 10 PM
                      - if:
                          - condition: time
                            after: 22:00
                        then:
                          - action: logger
                            message: Time is after 10 PM
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that satisfies the second "if" block condition (after 10 PM)
        TimeBasedEvent eventAfter10PM = new TimeBasedEvent(LocalTime.of(22, 30));

        // Process event
        engine.publishEvent(eventAfter10PM);

        // Assert: Should trigger the second "if" block
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Time is after 10 PM"))
                .noneMatch(msg -> msg.contains("Time is before 10 PM"));
    }

    @Test
    void testIfThenElseWithNoConditionsSatisfied() {
        var yaml = """
                alias: If-Then-Else with No Conditions Satisfied Test
                triggers:
                  - trigger: time
                    at: 21:30
                actions:
                  - action: ifThenElse
                    ifs:
                      - if:
                          - condition: time
                            after: 23:00
                        then:
                          - action: logger
                            message: Time is after 11 PM
                      - if:
                          - condition: time
                            after: 22:00
                        then:
                          - action: logger
                            message: Time is after 10 PM
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that satisfies none of the conditions (before 10 PM)
        TimeBasedEvent eventBefore10PM = new TimeBasedEvent(LocalTime.of(21, 30));

        // Process event
        engine.publishEvent(eventBefore10PM);

        // Assert: Should not trigger any "if" blocks
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Time is after 11 PM"))
                .noneMatch(msg -> msg.contains("Time is after 10 PM"));
    }

    @Test
    void testIfThenElseWithAllIfConditionsFail() {
        var yaml = """
                alias: If-Then-Else with All If Conditions Failing Test
                triggers:
                  - trigger: time
                    at: 20:45
                actions:
                  - action: ifThenElse
                    ifs:
                      - if:
                          - condition: time
                            after: 22:00
                        then:
                          - action: logger
                            message: Time is after 10 PM
                      - if:
                          - condition: time
                            after: 22:00
                        then:
                          - action: logger
                            message: Time is after 10 PM
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that fails all conditions (before 9:00 PM)
        TimeBasedEvent eventBefore9PM = new TimeBasedEvent(LocalTime.of(20, 45));

        // Process event
        engine.publishEvent(eventBefore9PM);

        // Assert: Should not trigger any "if" blocks
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Time is after 10 PM"))
                .noneMatch(msg -> msg.contains("Time is after 10 PM"));
    }

    @Test
    void testIfThenElseWithAllIfConditionsSatisfiedInFirstBlock() {
        var yaml = """
                alias: If-Then-Else with All If Conditions Satisfied in First Block Test
                triggers:
                  - trigger: time
                    at: 22:30
                actions:
                  - action: ifThenElse
                    ifs:
                      - if:
                          - condition: time
                            after: 22:00
                        then:
                          - action: logger
                            message: Time is after 10 PM
                      - if:
                          - condition: time
                            before: 23:00
                        then:
                          - action: logger
                            message: Time is before 11 PM
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create event that satisfies all conditions in the first block (after 10 PM but before 11 PM)
        TimeBasedEvent eventAfter10Before11PM = new TimeBasedEvent(LocalTime.of(22, 30));

        // Process event
        engine.publishEvent(eventAfter10Before11PM);

        // Assert: Should trigger the first "if" block
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Time is after 10 PM"))
                .noneMatch(msg -> msg.contains("Time is before 11 PM"));
    }
}