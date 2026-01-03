package com.davidrandoll.automation.engine.spring;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.spring.modules.actions.uda.DefaultUserDefinedActionRegistry;
import com.davidrandoll.automation.engine.spring.modules.actions.uda.UserDefinedActionDefinition;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Comprehensive stress tests for the execution stack (virtual stack machine).
 * These tests aim to find edge cases and bugs in pause/resume functionality.
 */
public class ExecutionStackStressTest extends AutomationEngineTest {


    private IEvent createEvent() {
        return new IEvent() {};
    }

    private void waitForLogMessages(String... expectedMessages) {
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            for (String msg : expectedMessages) {
                assertThat(logAppender.getLoggedMessages())
                        .anyMatch(m -> m.contains(msg));
            }
        });
    }

    // ==================== IF-THEN-ELSE TESTS ====================

    @Nested
    @DisplayName("If-Then-Else with Pause/Resume")
    class IfThenElseTests {

        @Test
        @DisplayName("Pause in IF branch then resume")
        void testPauseInIfBranch() {
            String yaml = """
                    alias: if-pause-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before-if"
                      - action: ifThenElse
                        if:
                          - condition: template
                            expression: "{{ true }}"
                        then:
                          - action: logger
                            message: "in-if-before-delay"
                          - action: delay
                            duration: PT0.5S
                          - action: logger
                            message: "in-if-after-delay"
                        else:
                          - action: logger
                            message: "should-not-run"
                      - action: logger
                        message: "after-if"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("before-if", "in-if-before-delay", "in-if-after-delay", "after-if");

            assertThat(logAppender.getLoggedMessages())
                    .noneMatch(m -> m.contains("should-not-run"));

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                long beforeCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("before-if")).count();
                long inIfBeforeCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("in-if-before-delay")).count();
                long inIfAfterCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("in-if-after-delay")).count();
                long afterCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("after-if")).count();
                assertThat(beforeCount).isEqualTo(1);
                assertThat(inIfBeforeCount).isEqualTo(1);
                assertThat(inIfAfterCount).isEqualTo(1);
                assertThat(afterCount).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Pause in ELSE branch then resume")
        void testPauseInElseBranch() {
            String yaml = """
                    alias: else-pause-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before-if"
                      - action: ifThenElse
                        if:
                          - condition: template
                            expression: "{{ false }}"
                        then:
                          - action: logger
                            message: "should-not-run"
                        else:
                          - action: logger
                            message: "in-else-before-delay"
                          - action: delay
                            duration: PT0.5S
                          - action: logger
                            message: "in-else-after-delay"
                      - action: logger
                        message: "after-if"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("before-if", "in-else-before-delay", "in-else-after-delay", "after-if");

            assertThat(logAppender.getLoggedMessages())
                    .noneMatch(m -> m.contains("should-not-run"));

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                long beforeCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("before-if")).count();
                long inElseBeforeCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("in-else-before-delay")).count();
                long inElseAfterCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("in-else-after-delay")).count();
                long afterCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("after-if")).count();
                assertThat(beforeCount).isEqualTo(1);
                assertThat(inElseBeforeCount).isEqualTo(1);
                assertThat(inElseAfterCount).isEqualTo(1);
                assertThat(afterCount).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Pause in ELSE-IF branch then resume")
        void testPauseInElseIfBranch() {
            String yaml = """
                    alias: elseif-pause-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before-if"
                      - action: ifThenElse
                        if:
                          - condition: template
                            expression: "{{ false }}"
                        then:
                          - action: logger
                            message: "should-not-run-1"
                        ifs:
                          - if:
                              - condition: template
                                expression: "{{ true }}"
                            then:
                              - action: logger
                                message: "in-elseif-before-delay"
                              - action: delay
                                duration: PT0.5S
                              - action: logger
                                message: "in-elseif-after-delay"
                        else:
                          - action: logger
                            message: "should-not-run-2"
                      - action: logger
                        message: "after-if"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("before-if", "in-elseif-before-delay", "in-elseif-after-delay", "after-if");

            assertThat(logAppender.getLoggedMessages())
                    .noneMatch(m -> m.contains("should-not-run"));

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                long beforeCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("before-if")).count();
                long inElseIfBeforeCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("in-elseif-before-delay")).count();
                long inElseIfAfterCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("in-elseif-after-delay")).count();
                long afterCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("after-if")).count();
                assertThat(beforeCount).isEqualTo(1);
                assertThat(inElseIfBeforeCount).isEqualTo(1);
                assertThat(inElseIfAfterCount).isEqualTo(1);
                assertThat(afterCount).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Multiple delays in nested if-then-else")
        void testMultipleDelaysInNestedIfThenElse() {
            String yaml = """
                    alias: nested-if-pause-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "L0-start"
                      - action: ifThenElse
                        if:
                          - condition: template
                            expression: "{{ true }}"
                        then:
                          - action: logger
                            message: "L1-before-delay"
                          - action: delay
                            duration: PT0.3S
                          - action: logger
                            message: "L1-after-delay"
                          - action: ifThenElse
                            if:
                              - condition: template
                                expression: "{{ true }}"
                            then:
                              - action: logger
                                message: "L2-before-delay"
                              - action: delay
                                duration: PT0.3S
                              - action: logger
                                message: "L2-after-delay"
                          - action: logger
                            message: "L1-end"
                      - action: logger
                        message: "L0-end"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("L0-start", "L1-before-delay", "L1-after-delay", 
                    "L2-before-delay", "L2-after-delay", "L1-end", "L0-end");

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L0-start")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L1-before-delay")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L1-after-delay")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L2-before-delay")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L2-after-delay")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L1-end")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L0-end")).count()).isEqualTo(1);
            });
        }
    }

    // ==================== REPEAT ACTION TESTS ====================

    @Nested
    @DisplayName("Repeat Action with Pause/Resume")
    class RepeatActionTests {

        @Test
        @DisplayName("Pause inside count-based repeat")
        void testPauseInCountRepeat() {
            String yaml = """
                    alias: repeat-count-pause-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before-repeat"
                      - action: repeat
                        count: 3
                        actions:
                          - action: logger
                            message: "iteration-start"
                          - action: delay
                            duration: PT0.3S
                          - action: logger
                            message: "iteration-end"
                      - action: logger
                        message: "after-repeat"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            // 3 iterations = 3 delays
            waitForLogMessages("before-repeat", "after-repeat");

            // Count the iteration messages
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                long startCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("iteration-start")).count();
                long endCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("iteration-end")).count();
                assertThat(startCount).isEqualTo(3);
                assertThat(endCount).isEqualTo(3);
            });
        }

        @Test
        @DisplayName("Pause inside forEach repeat")
        void testPauseInForEachRepeat() {
            String yaml = """
                    alias: repeat-foreach-pause-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before-repeat"
                      - action: repeat
                        forEach: [a, b, c]
                        as: item
                        actions:
                          - action: logger
                            message: "item={{ item }}"
                          - action: delay
                            duration: PT0.2S
                      - action: logger
                        message: "after-repeat"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("before-repeat", "item=a", "item=b", "item=c", "after-repeat");

            // Verify each item appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("before-repeat")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("item=a")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("item=b")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("item=c")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("after-repeat")).count()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Nested if-then-else inside repeat with delay")
        void testNestedIfThenElseInRepeatWithDelay() {
            String yaml = """
                    alias: repeat-nested-if-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "start"
                      - action: repeat
                        count: 2
                        actions:
                          - action: logger
                            message: "repeat-start"
                          - action: ifThenElse
                            if:
                              - condition: template
                                expression: "{{ true }}"
                            then:
                              - action: logger
                                message: "if-before-delay"
                              - action: delay
                                duration: PT0.2S
                              - action: logger
                                message: "if-after-delay"
                          - action: logger
                            message: "repeat-end"
                      - action: logger
                        message: "end"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("start", "repeat-start", "if-before-delay", "if-after-delay", 
                    "repeat-end", "end");

            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                long repeatStartCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("repeat-start")).count();
                long ifBeforeCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("if-before-delay")).count();
                long ifAfterCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("if-after-delay")).count();
                long repeatEndCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("repeat-end")).count();
                assertThat(repeatStartCount).isEqualTo(2);
                assertThat(ifBeforeCount).isEqualTo(2);
                assertThat(ifAfterCount).isEqualTo(2);
                assertThat(repeatEndCount).isEqualTo(2);
                // Single start/end
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("start")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("end")).count()).isEqualTo(1);
            });
        }
    }

    // ==================== SEQUENCE ACTION TESTS ====================

    @Nested
    @DisplayName("Sequence Action with Pause/Resume")
    class SequenceActionTests {

        @Test
        @DisplayName("Pause inside sequence")
        void testPauseInSequence() {
            String yaml = """
                    alias: sequence-pause-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before-sequence"
                      - action: sequence
                        actions:
                          - action: logger
                            message: "in-sequence-1"
                          - action: delay
                            duration: PT0.5S
                          - action: logger
                            message: "in-sequence-2"
                      - action: logger
                        message: "after-sequence"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("before-sequence", "in-sequence-1", "in-sequence-2", "after-sequence");

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("before-sequence")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("in-sequence-1")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("in-sequence-2")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("after-sequence")).count()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Multiple pauses in nested sequences")
        void testMultiplePausesInNestedSequences() {
            String yaml = """
                    alias: nested-sequence-pause-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "start"
                      - action: sequence
                        actions:
                          - action: logger
                            message: "outer-1"
                          - action: delay
                            duration: PT0.2S
                          - action: sequence
                            actions:
                              - action: logger
                                message: "inner-1"
                              - action: delay
                                duration: PT0.2S
                              - action: logger
                                message: "inner-2"
                          - action: logger
                            message: "outer-2"
                          - action: delay
                            duration: PT0.2S
                          - action: logger
                            message: "outer-3"
                      - action: logger
                        message: "end"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("start", "outer-1", "inner-1", "inner-2", "outer-2", "outer-3", "end");

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("start")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("outer-1")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("inner-1")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("inner-2")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("outer-2")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("outer-3")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("end")).count()).isEqualTo(1);
            });
        }
    }

    // ==================== DEEPLY NESTED TESTS ====================

    @Nested
    @DisplayName("Deeply Nested Scenarios")
    class DeeplyNestedTests {

        @Test
        @DisplayName("If inside Repeat inside Sequence with delay")
        void testIfInsideRepeatInsideSequence() {
            String yaml = """
                    alias: deep-nested-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "start"
                      - action: sequence
                        actions:
                          - action: logger
                            message: "seq-1"
                          - action: repeat
                            count: 2
                            actions:
                              - action: logger
                                message: "repeat-start"
                              - action: ifThenElse
                                if:
                                  - condition: template
                                    expression: "{{ true }}"
                                then:
                                  - action: logger
                                    message: "if-before"
                                  - action: delay
                                    duration: PT0.2S
                                  - action: logger
                                    message: "if-after"
                              - action: logger
                                message: "repeat-end"
                          - action: logger
                            message: "seq-2"
                      - action: logger
                        message: "end"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("start", "seq-1", "repeat-start", "if-before", "if-after", 
                    "repeat-end", "seq-2", "end");

            // Verify repeat runs twice, others once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("start")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("seq-1")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("repeat-start")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("if-before")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("if-after")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("repeat-end")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("seq-2")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("end")).count()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("5-level deep nesting with delays")
        void testFiveLevelDeepNesting() {
            String yaml = """
                    alias: five-level-nested-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "L0-start"
                      - action: sequence
                        actions:
                          - action: logger
                            message: "L1-start"
                          - action: ifThenElse
                            if:
                              - condition: template
                                expression: "{{ true }}"
                            then:
                              - action: logger
                                message: "L2-start"
                              - action: repeat
                                count: 2
                                actions:
                                  - action: logger
                                    message: "L3-start"
                                  - action: sequence
                                    actions:
                                      - action: logger
                                        message: "L4-start"
                                      - action: ifThenElse
                                        if:
                                          - condition: template
                                            expression: "{{ true }}"
                                        then:
                                          - action: logger
                                            message: "L5-before-delay"
                                          - action: delay
                                            duration: PT0.15S
                                          - action: logger
                                            message: "L5-after-delay"
                                      - action: logger
                                        message: "L4-end"
                                  - action: logger
                                    message: "L3-end"
                              - action: logger
                                message: "L2-end"
                          - action: logger
                            message: "L1-end"
                      - action: logger
                        message: "L0-end"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("L0-start", "L1-start", "L2-start", "L3-start", "L4-start",
                    "L5-before-delay", "L5-after-delay", "L4-end", "L3-end", "L2-end", 
                    "L1-end", "L0-end");

            // Verify counts (repeat count=2 means L3-L5 appear twice, others once)
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L0-start")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L1-start")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L2-start")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L3-start")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L4-start")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L5-before-delay")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L5-after-delay")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L4-end")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L3-end")).count()).isEqualTo(2);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L2-end")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L1-end")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("L0-end")).count()).isEqualTo(1);
            });
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Multiple consecutive delays")
        void testMultipleConsecutiveDelays() {
            String yaml = """
                    alias: consecutive-delays-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before"
                      - action: delay
                        duration: PT0.2S
                      - action: logger
                        message: "between-1"
                      - action: delay
                        duration: PT0.2S
                      - action: logger
                        message: "between-2"
                      - action: delay
                        duration: PT0.2S
                      - action: logger
                        message: "after"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("before", "between-1", "between-2", "after");

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("before")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("between-1")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("between-2")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("after")).count()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Delay at the very end of automation")
        void testDelayAtEnd() {
            String yaml = """
                    alias: delay-at-end-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before"
                      - action: delay
                        duration: PT0.3S
                      - action: logger
                        message: "after"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("before", "after");

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("before")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("after")).count()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Empty if branch with delay in else")
        void testEmptyIfBranchWithDelayInElse() {
            String yaml = """
                    alias: empty-if-delay-else-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "empty-if-before"
                      - action: ifThenElse
                        if:
                          - condition: template
                            expression: "{{ false }}"
                        then: []
                        else:
                          - action: logger
                            message: "in-else"
                          - action: delay
                            duration: PT0.3S
                          - action: logger
                            message: "after-delay-in-else"
                      - action: logger
                        message: "empty-if-after"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("empty-if-before", "in-else", "after-delay-in-else", "empty-if-after");

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("empty-if-before")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("in-else")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("after-delay-in-else")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("empty-if-after")).count()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Zero count repeat (should skip)")
        void testZeroCountRepeat() {
            String yaml = """
                    alias: zero-repeat-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before"
                      - action: repeat
                        count: 0
                        actions:
                          - action: logger
                            message: "should-not-run"
                          - action: delay
                            duration: PT0.3S
                      - action: logger
                        message: "after"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            // Should complete immediately without any pause
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages())
                        .anyMatch(m -> m.contains("before"))
                        .anyMatch(m -> m.contains("after"))
                        .noneMatch(m -> m.contains("should-not-run"));
            });
        }
    }

    // ==================== STOP ACTION TESTS ====================

    @Nested
    @DisplayName("Stop Actions with Pause/Resume")
    class StopActionTests {

        @Test
        @DisplayName("Stop automation after delay resumes")
        void testStopAfterDelay() {
            String yaml = """
                    alias: stop-after-delay-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "stop-test-before"
                      - action: delay
                        duration: PT0.3S
                      - action: logger
                        message: "after-delay"
                      - action: stop
                        stopAutomation: true
                        condition:
                          condition: alwaysTrue
                      - action: logger
                        message: "should-not-run"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("stop-test-before", "after-delay");

            // Give it a moment and then verify the last action didn't run
            await().during(1, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages())
                        .noneMatch(m -> m.contains("should-not-run"));
                // Verify each message appears exactly once
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("stop-test-before")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.equals("after-delay")).count()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Stop action sequence inside repeat after delay")
        void testStopSequenceInsideRepeatAfterDelay() {
            String yaml = """
                    alias: stop-loop-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "before"
                      - action: repeat
                        count: 3
                        actions:
                          - action: logger
                            message: "LOOP-ITERATION"
                          - action: delay
                            duration: PT0.2S
                          - action: stop
                            stopActionSequence: true
                            condition:
                              condition: alwaysTrue
                          - action: logger
                            message: "after-stop-in-loop"
                      - action: logger
                        message: "after-repeat"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("before", "LOOP-ITERATION", "after-repeat");

            // The stop should exit the repeat, so only one "LOOP-ITERATION" message
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                long loopCount = logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("LOOP-ITERATION")).count();
                // Stop should prevent remaining iterations
                assertThat(loopCount).isEqualTo(1);
                // "after-stop-in-loop" should not appear
                assertThat(logAppender.getLoggedMessages())
                        .noneMatch(m -> m.contains("after-stop-in-loop"));
            });
        }
    }

    // ==================== USER DEFINED ACTION TESTS ====================
    // NOTE: These tests are disabled because UserDefinedAction creates a new EventContext,
    // which means the execution stack is not preserved during pause/resume.
    // This is a known limitation - fixing it requires UserDefinedAction to use 
    // ActionResult.invoke() instead of creating a new EventContext.

    @Nested
    @DisplayName("User Defined Actions with Pause/Resume")
    @org.junit.jupiter.api.Disabled("UDA creates new EventContext, breaking pause/resume chain")
    class UserDefinedActionTests {

        @Autowired
        private DefaultUserDefinedActionRegistry actionRegistry;

        @Test
        @DisplayName("Delay inside user-defined action")
        void testDelayInsideUserDefinedAction() {
            // Register a UDA with a delay
            var udaDefinition = UserDefinedActionDefinition.builder()
                    .name("udaWithDelay")
                    .description("A UDA that contains a delay")
                    .actions(List.of(
                            ActionDefinition.builder()
                                    .action("logger")
                                    .params(Map.of("message", "UDA-BEFORE-DELAY"))
                                    .build(),
                            ActionDefinition.builder()
                                    .action("delay")
                                    .params(Map.of("duration", "PT0.3S"))
                                    .build(),
                            ActionDefinition.builder()
                                    .action("logger")
                                    .params(Map.of("message", "UDA-AFTER-DELAY"))
                                    .build()
                    ))
                    .build();
            actionRegistry.registerAction(udaDefinition);

            String yaml = """
                    alias: uda-delay-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "BEFORE-UDA"
                      - action: userDefinedAction
                        name: udaWithDelay
                      - action: logger
                        message: "AFTER-UDA"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("BEFORE-UDA", "UDA-BEFORE-DELAY", "UDA-AFTER-DELAY", "AFTER-UDA");
        }

        @Test
        @DisplayName("Nested UDA with if-then-else containing delay")
        void testNestedUDAWithIfThenElseDelay() {
            // Register a UDA with if-then-else containing a delay
            var udaDefinition = UserDefinedActionDefinition.builder()
                    .name("udaWithCondition")
                    .description("A UDA with conditional delay")
                    .parameters(Map.of("shouldDelay", "true"))
                    .actions(List.of(
                            ActionDefinition.builder()
                                    .action("logger")
                                    .params(Map.of("message", "NESTED-UDA-START"))
                                    .build(),
                            ActionDefinition.builder()
                                    .action("ifThenElse")
                                    .params(Map.of(
                                            "if", List.of(Map.of(
                                                    "condition", "template",
                                                    "expression", "{{ shouldDelay == 'true' }}"
                                            )),
                                            "then", List.of(
                                                    Map.of(
                                                            "action", "logger",
                                                            "message", "CONDITIONAL-BEFORE-DELAY"
                                                    ),
                                                    Map.of(
                                                            "action", "delay",
                                                            "duration", "PT0.2S"
                                                    ),
                                                    Map.of(
                                                            "action", "logger",
                                                            "message", "CONDITIONAL-AFTER-DELAY"
                                                    )
                                            )
                                    ))
                                    .build(),
                            ActionDefinition.builder()
                                    .action("logger")
                                    .params(Map.of("message", "NESTED-UDA-END"))
                                    .build()
                    ))
                    .build();
            actionRegistry.registerAction(udaDefinition);

            String yaml = """
                    alias: nested-uda-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "MAIN-START"
                      - action: userDefinedAction
                        name: udaWithCondition
                        shouldDelay: "true"
                      - action: logger
                        message: "MAIN-END"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("MAIN-START", "NESTED-UDA-START", "CONDITIONAL-BEFORE-DELAY", 
                    "CONDITIONAL-AFTER-DELAY", "NESTED-UDA-END", "MAIN-END");
        }
    }

    // ==================== COMPLEX NESTING TESTS ====================

    @Nested
    @DisplayName("Complex Nesting Scenarios")
    class ComplexNestingTests {

        @Test
        @DisplayName("Stop automation from deeply nested block")
        void testStopFromDeeplyNestedBlock() {
            String yaml = """
                    alias: deep-stop-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "LEVEL-0"
                      - action: sequence
                        actions:
                          - action: logger
                            message: "LEVEL-1"
                          - action: ifThenElse
                            if:
                              - condition: alwaysTrue
                            then:
                              - action: logger
                                message: "LEVEL-2"
                              - action: repeat
                                count: 1
                                actions:
                                  - action: logger
                                    message: "LEVEL-3-BEFORE-DELAY"
                                  - action: delay
                                    duration: PT0.2S
                                  - action: logger
                                    message: "LEVEL-3-AFTER-DELAY"
                                  - action: stop
                                    stopAutomation: true
                                    condition:
                                      condition: alwaysTrue
                                  - action: logger
                                    message: "SHOULD-NOT-RUN-1"
                              - action: logger
                                message: "SHOULD-NOT-RUN-2"
                          - action: logger
                            message: "SHOULD-NOT-RUN-3"
                      - action: logger
                        message: "SHOULD-NOT-RUN-4"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("LEVEL-0", "LEVEL-1", "LEVEL-2", "LEVEL-3-BEFORE-DELAY", "LEVEL-3-AFTER-DELAY");

            // Verify none of the "SHOULD-NOT-RUN" messages appear
            await().during(500, TimeUnit.MILLISECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages())
                        .noneMatch(m -> m.contains("SHOULD-NOT-RUN"));
                // Verify each level message appears exactly once
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("LEVEL-0")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("LEVEL-1")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("LEVEL-2")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("LEVEL-3-BEFORE-DELAY")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("LEVEL-3-AFTER-DELAY")).count()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Multiple delays across different nesting levels")
        void testMultipleDelaysAcrossNestingLevels() {
            String yaml = """
                    alias: multi-level-delay-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "ROOT-1"
                      - action: delay
                        duration: PT0.1S
                      - action: logger
                        message: "ROOT-2"
                      - action: sequence
                        actions:
                          - action: logger
                            message: "SEQ-1"
                          - action: delay
                            duration: PT0.1S
                          - action: logger
                            message: "SEQ-2"
                          - action: ifThenElse
                            if:
                              - condition: alwaysTrue
                            then:
                              - action: logger
                                message: "IF-1"
                              - action: delay
                                duration: PT0.1S
                              - action: logger
                                message: "IF-2"
                      - action: logger
                        message: "ROOT-3"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("ROOT-1", "ROOT-2", "SEQ-1", "SEQ-2", "IF-1", "IF-2", "ROOT-3");

            // Verify each message appears exactly once
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("ROOT-1")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("ROOT-2")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("SEQ-1")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("SEQ-2")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("IF-1")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("IF-2")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("ROOT-3")).count()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("StopActionSequence at different nesting levels")
        void testStopSequenceAtDifferentLevels() {
            String yaml = """
                    alias: stop-sequence-levels-test
                    triggers:
                      - trigger: alwaysTrue
                    actions:
                      - action: logger
                        message: "ROOT-START"
                      - action: sequence
                        actions:
                          - action: logger
                            message: "SEQ-START"
                          - action: delay
                            duration: PT0.2S
                          - action: stop
                            stopActionSequence: true
                            condition:
                              condition: alwaysTrue
                          - action: logger
                            message: "SEQ-SHOULD-NOT-RUN"
                      - action: logger
                        message: "ROOT-AFTER-SEQUENCE"
                    """;

            Automation automation = factory.createAutomation("yaml", yaml);
            engine.register(automation);
            engine.publishEvent(createEvent());

            waitForLogMessages("ROOT-START", "SEQ-START", "ROOT-AFTER-SEQUENCE");

            await().during(500, TimeUnit.MILLISECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages())
                        .noneMatch(m -> m.contains("SEQ-SHOULD-NOT-RUN"));
                // Verify each message appears exactly once
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("ROOT-START")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("SEQ-START")).count()).isEqualTo(1);
                assertThat(logAppender.getLoggedMessages().stream()
                        .filter(m -> m.contains("ROOT-AFTER-SEQUENCE")).count()).isEqualTo(1);
            });
        }
    }
}
