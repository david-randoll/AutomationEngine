package com.davidrandoll.automation.engine.spring;

import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for the PauseUntilAction feature.
 * Tests event-driven pause/resume with various trigger conditions.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PauseUntilIntegrationTest extends AutomationEngineTest {

    @BeforeEach
    void setUpTest() {
        logAppender.clear();
    }

    @Nested
    @DisplayName("Basic PauseUntil Tests")
    class BasicPauseUntilTests {

        @Test
        @DisplayName("Should pause and resume on matching event")
        void testBasicPauseUntilResume() {
            String yaml = """
                    alias: pause-until-approval
                    triggers:
                      - trigger: onEvent
                        eventType: OrderPlacedEvent
                    actions:
                      - action: logger
                        message: "Order received"
                      - action: pauseUntil
                        trigger:
                          trigger: onEvent
                          eventType: ApprovalEvent
                      - action: logger
                        message: "Order approved"
                    """;

            engine.registerWithYaml(yaml);

            // Trigger the automation
            engine.publishEvent(new OrderPlacedEvent("order-123"));

            // Wait for first log
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.equals("Order received")));

            // Should NOT have second log yet
            assertThat(logAppender.getLoggedMessages())
                    .noneMatch(m -> m.equals("Order approved"));

            // Send approval event to resume
            engine.publishEvent(new ApprovalEvent("order-123"));

            // Now should have second log
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.equals("Order approved")));
        }

        @Test
        @DisplayName("Should handle conditional resume trigger")
        void testConditionalResumeTrigger() {
            String yaml = """
                    alias: conditional-pause-until
                    triggers:
                      - trigger: onEvent
                        eventType: RequestEvent
                    actions:
                      - action: logger
                        message: "Request {{ event.requestId }} started"
                      - action: pauseUntil
                        trigger:
                          trigger: template
                          expression: "{{ event.requestId == trigger.requestId and event.status == 'completed' }}"
                      - action: logger
                        message: "Request {{ event.requestId }} completed"
                    """;

            engine.registerWithYaml(yaml);

            // Start request
            engine.publishEvent(new RequestEvent("req-456", "pending"));

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.contains("req-456 started")));

            // Send non-matching event (different requestId)
            engine.publishEvent(new RequestEvent("req-999", "completed"));

            // Should NOT resume
            await().pollDelay(500, TimeUnit.MILLISECONDS)
                    .atMost(1, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            assertThat(logAppender.getLoggedMessages())
                                    .noneMatch(m -> m.contains("req-456 completed")));

            // Send matching event (same requestId, status=completed)
            engine.publishEvent(new RequestEvent("req-456", "completed"));

            // Now should resume
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.contains("req-456 completed")));
        }
    }

    @Nested
    @DisplayName("Multiple Paused Executions")
    class MultiplePausedExecutionsTests {

        @Test
        @DisplayName("Should handle multiple paused executions independently")
        void testMultiplePausedExecutions() {
            String yaml = """
                    alias: multi-pause-test
                    triggers:
                      - trigger: onEvent
                        eventType: OrderPlacedEvent
                    actions:
                      - action: logger
                        message: "Order {{ event.orderId }} started"
                      - action: pauseUntil
                        trigger:
                          trigger: template
                          expression: "{{ event.orderId == trigger.orderId }}"
                      - action: logger
                        message: "Order {{ event.orderId }} resumed"
                    """;

            engine.registerWithYaml(yaml);

            // Start multiple orders
            engine.publishEvent(new OrderPlacedEvent("A"));
            engine.publishEvent(new OrderPlacedEvent("B"));
            engine.publishEvent(new OrderPlacedEvent("C"));

            // All should be paused
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages())
                        .anyMatch(m -> m.equals("Order A started"));
                assertThat(logAppender.getLoggedMessages())
                        .anyMatch(m -> m.equals("Order B started"));
                assertThat(logAppender.getLoggedMessages())
                        .anyMatch(m -> m.equals("Order C started"));
            });

            // Resume order B
            engine.publishEvent(new ApprovalEvent("B"));

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.equals("Order B resumed")));

            // A and C should still be paused
            assertThat(logAppender.getLoggedMessages())
                    .noneMatch(m -> m.equals("Order A resumed"));
            assertThat(logAppender.getLoggedMessages())
                    .noneMatch(m -> m.equals("Order C resumed"));

            // Resume order A
            engine.publishEvent(new ApprovalEvent("A"));

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.equals("Order A resumed")));
        }
    }

    @Nested
    @DisplayName("Timeout Tests")
    class TimeoutTests {

        @Test
        @DisplayName("Should timeout after specified duration")
        void testPauseUntilTimeout() {
            String yaml = """
                    alias: timeout-test
                    triggers:
                      - trigger: onEvent
                        eventType: OrderPlacedEvent
                    actions:
                      - action: logger
                        message: "Order received"
                      - action: pauseUntil
                        trigger:
                          trigger: onEvent
                          eventType: ApprovalEvent
                        timeoutMillis: 1000
                      - action: logger
                        message: "Order approved"
                    """;

            engine.registerWithYaml(yaml);

            engine.publishEvent(new OrderPlacedEvent("timeout-order"));

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.equals("Order received")));

            // Wait for timeout + buffer
            await().pollDelay(1500, TimeUnit.MILLISECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            assertThat(logAppender.getLoggedMessages())
                                    .anyMatch(m -> m.contains("timed out")));

            // Even if approval comes now, it shouldn't resume (timed out)
            engine.publishEvent(new ApprovalEvent("timeout-order"));

            await().pollDelay(500, TimeUnit.MILLISECONDS)
                    .atMost(1, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            assertThat(logAppender.getLoggedMessages())
                                    .noneMatch(m -> m.equals("Order approved")));
        }
    }

    @Nested
    @DisplayName("Complex Workflow Tests")
    class ComplexWorkflowTests {

        @Test
        @DisplayName("Should support multiple pause points in one automation")
        void testMultiplePausePoints() {
            String yaml = """
                    alias: multi-stage-approval
                    triggers:
                      - trigger: onEvent
                        eventType: OrderPlacedEvent
                    actions:
                      - action: logger
                        message: "Stage 1: Order placed"
                      - action: pauseUntil
                        pauseId: "manager-approval"
                        trigger:
                          trigger: onEvent
                          eventType: ManagerApprovalEvent
                      - action: logger
                        message: "Stage 2: Manager approved"
                      - action: pauseUntil
                        pauseId: "finance-approval"
                        trigger:
                          trigger: onEvent
                          eventType: FinanceApprovalEvent
                      - action: logger
                        message: "Stage 3: Finance approved - Order complete"
                    """;

            engine.registerWithYaml(yaml);

            engine.publishEvent(new OrderPlacedEvent("multi-stage"));

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.equals("Stage 1: Order placed")));

            // Manager approves
            engine.publishEvent(new ManagerApprovalEvent());

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.equals("Stage 2: Manager approved")));

            // Not complete yet
            assertThat(logAppender.getLoggedMessages())
                    .noneMatch(m -> m.contains("Order complete"));

            // Finance approves
            engine.publishEvent(new FinanceApprovalEvent());

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.equals("Stage 3: Finance approved - Order complete")));
        }

        @Test
        @DisplayName("Should work with nested control structures")
        void testPauseUntilInSequence() {
            String yaml = """
                    alias: nested-pause
                    triggers:
                      - trigger: onEvent
                        eventType: OrderPlacedEvent
                    actions:
                      - action: sequence
                        actions:
                          - action: logger
                            message: "SEQ-1"
                          - action: pauseUntil
                            trigger:
                              trigger: onEvent
                              eventType: ApprovalEvent
                          - action: logger
                            message: "SEQ-2"
                      - action: logger
                        message: "AFTER-SEQUENCE"
                    """;

            engine.registerWithYaml(yaml);

            engine.publishEvent(new OrderPlacedEvent("nested"));

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(logAppender.getLoggedMessages())
                            .anyMatch(m -> m.equals("SEQ-1")));

            // Should be paused
            assertThat(logAppender.getLoggedMessages())
                    .noneMatch(m -> m.equals("SEQ-2"));

            // Resume
            engine.publishEvent(new ApprovalEvent("nested"));

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(logAppender.getLoggedMessages())
                        .anyMatch(m -> m.equals("SEQ-2"));
                assertThat(logAppender.getLoggedMessages())
                        .anyMatch(m -> m.equals("AFTER-SEQUENCE"));
            });
        }
    }

    // ---- Test Event Classes ----

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class OrderPlacedEvent implements IEvent {
        private final String orderId;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ApprovalEvent implements IEvent {
        private final String orderId;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class RequestEvent implements IEvent {
        private final String requestId;
        private final String status;
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class ManagerApprovalEvent implements IEvent {
        // Empty approval event
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class FinanceApprovalEvent implements IEvent {
        // Empty approval event
    }
}

