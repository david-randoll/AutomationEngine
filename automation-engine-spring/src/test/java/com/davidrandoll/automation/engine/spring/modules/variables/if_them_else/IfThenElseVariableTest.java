package com.davidrandoll.automation.engine.spring.modules.variables.if_them_else;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class IfThenElseVariableTest extends AutomationEngineTest {

    @Test
    void testConditionalVariableAssignmentAndLogging() {
        var yaml = """
                alias: Test Conditional Variable Assignment
                triggers:
                  - trigger: time
                    at: 18:00
                variables:
                  - variable: ifThenElse
                    if:
                      - condition: time
                        before: 18:30
                    then:
                      - status_message: "Its still early!"
                    else:
                      - status_message: "Its getting late!"
                actions:
                  - action: logger
                    message: "{{ status_message }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent eventAt18 = new TimeBasedEvent(LocalTime.of(18, 0));
        engine.publishEvent(eventAt18);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Its still early!"))
                .noneMatch(msg -> msg.contains("Its getting late!"));
    }

    @Test
    void testIfConditionMetThenLogCorrectMessage() {
        var yaml = """
                alias: If Then Action Test
                triggers:
                  - trigger: time
                    at: 22:37
                variables:
                  - variable: ifThenElse
                    if:
                      - condition: time
                        after: 22:30
                    then:
                      - status_message: "Condition satisfied, executed THEN actions."
                    else:
                      - status_message: "Executed ELSE actions."
                actions:
                  - action: logger
                    message: "{{ status_message }}"
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
    void testIfConditionNotMetThenLogElseMessage() {
        var yaml = """
                alias: If Then Else Action Test
                triggers:
                  - trigger: time
                    at: 22:37
                variables:
                  - variable: ifThenElse
                    if:
                      - condition: time
                        after: 23:00
                    then:
                      - status_message: "Condition satisfied, executed THEN actions."
                    else:
                      - status_message: "Executed ELSE actions."
                actions:
                  - action: logger
                    message: "{{ status_message }}"
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
    void testMultipleIfThenBlocksWithFirstConditionMetThenLogFirstMessage() {
        var yaml = """
                alias: Multiple If Then Blocks Test
                triggers:
                  - trigger: time
                    at: 22:37
                variables:
                  - variable: ifThenElse
                    if:
                      - condition: time
                        after: 22:30
                    then:
                      - status_message: "Condition satisfied in the first block, executed THEN actions."
                    ifs:
                      - if:
                          - condition: time
                            after: 23:00
                        then:
                          - status_message: "This block won't execute."
                    else:
                      - status_message: "Executed ELSE actions."
                actions:
                  - action: logger
                    message: "{{ status_message }}"
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
    void testMultipleIfThenBlocksWithNoConditionMetThenLogElseMessage() {
        var yaml = """
                alias: Multiple If Then Blocks No Condition Test
                triggers:
                  - trigger: time
                    at: 22:37
                variables:
                  - variable: ifThenElse
                    if:
                      - condition: time
                        after: 23:00
                    then:
                      - status_message: "This block won't execute."
                    ifs:
                      - if:
                          - condition: time
                            after: 23:30
                        then:
                          - status_message: "This block won't execute either."
                    else:
                      - status_message: "Executed ELSE actions."
                actions:
                  - action: logger
                    message: "{{ status_message }}"
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
    void testNestedIfThenElseWithMainAndNestedConditionsMet() {
        var yaml = """
                alias: Nested If Then Else Action Test
                triggers:
                    - trigger: time
                      at: 22:37
                variables:
                    - variable: ifThenElse
                      if:
                          - condition: time
                            after: 22:30
                      then:
                          - variable: ifThenElse
                            if:
                                - condition: time
                                  before: 23:00
                            then:
                                - status_message: "Nested condition met, executed nested THEN actions."
                            else:
                                - status_message: "Nested condition failed, executed nested ELSE actions."
                      else:
                          - status_message: "Main condition failed, executed main ELSE actions."
                actions:
                    - action: logger
                      message: "{{ status_message }}"
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
    void testIfThenElseWithMultipleConditionsSatisfied() {
        var yaml = """
                alias: If-Then-Else with Multiple If-Then Blocks Test
                triggers:
                  - trigger: time
                    at: 22:15
                variables:
                  - variable: ifThenElse
                    if:
                      - condition: time
                        after: 22:00
                    then:
                      - status_message: "Time is after 10 PM"
                    ifs:
                      - if:
                          - condition: time
                            before: 22:30
                        then:
                          - status_message: "Time is before 10:30 PM"
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - status_message: "Time is after 10:30 PM"
                    else:
                      - status_message: "Else condition triggered"
                actions:
                  - action: logger
                    message: "{{ status_message }}"
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
    void testIfThenElseWithMultipleIfThenBlocksNoConditionMet() {
        var yaml = """
                alias: If-Then-Else with Multiple If-Then Blocks Unsatisfied Test
                triggers:
                  - trigger: time
                    at: 09:45
                variables:
                  - variable: ifThenElse
                    if:
                      - condition: time
                        after: 22:00
                    then:
                      - status_message: "Time is after 10 PM"
                    ifs:
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - status_message: "Time is before 10:30 PM"
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - status_message: "Time is after 10:30 PM"
                    else:
                      - status_message: "Else condition triggered"
                actions:
                  - action: logger
                    message: "{{ status_message }}"
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
    void testIfThenElseWithNoConditionMetInIfBlocks() {
        var yaml = """
                alias: If-Then-Else with All If Conditions Failing Test
                triggers:
                  - trigger: time
                    at: 09:30
                variables:
                  - variable: ifThenElse
                    if:
                      - condition: time
                        after: 22:00
                    then:
                      - status_message: "Time is after 10 PM"
                    ifs:
                      - if:
                          - condition: time
                            after: 22:00
                        then:
                          - status_message: "Time is after 10:00 PM"
                      - if:
                          - condition: time
                            after: 22:30
                        then:
                          - status_message: "Time is after 10:30 PM"
                    else:
                      - status_message: "Else condition triggered"
                actions:
                  - action: logger
                    message: "{{ status_message }}"
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
    void testIfThenElseWithFirstIfBlockSatisfiedSecondBlockFails() {
        var yaml = """
                alias: If-Then-Else with First If Block Satisfied Test
                triggers:
                    - trigger: time
                      at: 22:35
                variables:
                    - variable: ifThenElse
                      if:
                          - condition: time
                            after: 22:00
                      then:
                          - status_message: "Time is after 10 PM"
                      ifs:
                          - if:
                                - condition: time
                                  after: 22:30
                            then:
                                - status_message: "Time is after 10:30 PM"
                          - if:
                                - condition: time
                                  before: 22:00
                            then:
                                - status_message: "Time is before 10 PM"
                      else:
                          - status_message: "Else condition triggered"
                actions:
                    - action: logger
                      message: "{{ status_message }}"
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
                   variables:
                       - variable: ifThenElse
                         ifs:
                             - if:
                                   - condition: time
                                     after: 22:30
                               then:
                                   - status_message: "Time is after 10:30 PM"
                         else:
                             - status_message: "Else condition triggered"
                   actions:
                       - action: logger
                         message: "{{ status_message }}"
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
                   variables:
                       - variable: ifThenElse
                         ifs:
                             - if:
                                   - condition: time
                                     after: 22:00
                               then:
                                   - status_message: "Time is after 10 PM"
                             - if:
                                   - condition: time
                                     after: 23:00
                               then:
                                   - status_message: "Time is after 11 PM"
                   actions:
                       - action: logger
                         message: "{{ status_message }}"
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
                   variables:
                       - variable: ifThenElse
                         ifs:
                             - if:
                                   - condition: time
                                     before: 22:00
                               then:
                                   - status_message: "Time is before 10 PM"
                             - if:
                                   - condition: time
                                     after: 22:00
                               then:
                                   - status_message: "Time is after 10 PM"
                   actions:
                       - action: logger
                         message: "{{ status_message }}"
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
                   variables:
                       - variable: ifThenElse
                         ifs:
                             - if:
                                   - condition: time
                                     after: 23:00
                               then:
                                   - status_message: "Time is after 11 PM"
                             - if:
                                   - condition: time
                                     after: 22:00
                               then:
                                   - status_message: "Time is after 10 PM"
                   actions:
                       - action: logger
                         message: "{{ status_message }}"
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
                   variables:
                       - variable: ifThenElse
                         ifs:
                             - if:
                                   - condition: time
                                     after: 22:00
                               then:
                                   - status_message: "Time is after 10 PM"
                             - if:
                                   - condition: time
                                     after: 22:00
                               then:
                                   - status_message: "Time is after 10 PM"
                   actions:
                       - action: logger
                         message: "{{ status_message }}"
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
                   variables:
                       - variable: ifThenElse
                         ifs:
                             - if:
                                   - condition: time
                                     after: 22:00
                               then:
                                   - status_message: "Time is after 10 PM"
                             - if:
                                   - condition: time
                                     before: 23:00
                               then:
                                   - status_message: "Time is before 11 PM"
                   actions:
                       - action: logger
                         message: "{{ status_message }}"
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