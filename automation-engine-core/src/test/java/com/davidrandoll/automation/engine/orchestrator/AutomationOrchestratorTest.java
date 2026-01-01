package com.davidrandoll.automation.engine.orchestrator;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.actions.BaseActionList;
import com.davidrandoll.automation.engine.core.conditions.BaseConditionList;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.publisher.AutomationEngineProcessedEvent;
import com.davidrandoll.automation.engine.core.events.publisher.AutomationEngineRegisterEvent;
import com.davidrandoll.automation.engine.core.events.publisher.AutomationEngineRemoveAllEvent;
import com.davidrandoll.automation.engine.core.events.publisher.AutomationEngineRemoveEvent;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.core.triggers.BaseTriggerList;
import com.davidrandoll.automation.engine.core.state.IStateStore;
import com.davidrandoll.automation.engine.core.state.InMemoryStateStore;
import com.davidrandoll.automation.engine.test.TestEvent;
import com.davidrandoll.automation.engine.test.mocks.MockEventPublisher;
import com.davidrandoll.automation.engine.test.mocks.SimpleAction;
import com.davidrandoll.automation.engine.test.mocks.SimpleCondition;
import com.davidrandoll.automation.engine.test.mocks.SimpleTrigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class AutomationOrchestratorTest {

    private MockEventPublisher eventPublisher;
    private IStateStore stateStore;
    private AutomationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        eventPublisher = new MockEventPublisher();
        stateStore = new InMemoryStateStore();
        orchestrator = new AutomationOrchestrator(eventPublisher, stateStore);
    }

    @Test
    void testRegisterAutomation_addsToList() {
        // Given
        Automation automation = createSimpleAutomation("test-automation");

        // When
        orchestrator.registerAutomation(automation);

        // Then
        assertThat(orchestrator.getAutomations()).hasSize(1);
        assertThat(orchestrator.getAutomations()).containsExactly(automation);
    }

    @Test
    void testRegisterAutomation_publishesRegisterEvent() {
        // Given
        Automation automation = createSimpleAutomation("test-automation");

        // When
        orchestrator.registerAutomation(automation);

        // Then
        List<AutomationEngineRegisterEvent> registerEvents = eventPublisher
                .getEventsOfType(AutomationEngineRegisterEvent.class);
        assertThat(registerEvents).hasSize(1);
        assertThat(registerEvents.getFirst().getAutomation()).isEqualTo(automation);
    }

    @Test
    void testRegisterAutomation_multipleAutomations() {
        // Given
        Automation automation1 = createSimpleAutomation("automation-1");
        Automation automation2 = createSimpleAutomation("automation-2");
        Automation automation3 = createSimpleAutomation("automation-3");

        // When
        orchestrator.registerAutomation(automation1);
        orchestrator.registerAutomation(automation2);
        orchestrator.registerAutomation(automation3);

        // Then
        assertThat(orchestrator.getAutomations()).hasSize(3);
        assertThat(orchestrator.getAutomations()).containsExactly(automation1, automation2, automation3);
    }

    @Test
    void testRemoveAutomation_removesFromList() {
        // Given
        Automation automation = createSimpleAutomation("test-automation");
        orchestrator.registerAutomation(automation);
        eventPublisher.clear();

        // When
        orchestrator.removeAutomation(automation);

        // Then
        assertThat(orchestrator.getAutomations()).isEmpty();
    }

    @Test
    void testRemoveAutomation_publishesRemoveEvent() {
        // Given
        Automation automation = createSimpleAutomation("test-automation");
        orchestrator.registerAutomation(automation);
        eventPublisher.clear();

        // When
        orchestrator.removeAutomation(automation);

        // Then
        List<AutomationEngineRemoveEvent> removeEvents = eventPublisher
                .getEventsOfType(AutomationEngineRemoveEvent.class);
        assertThat(removeEvents).hasSize(1);
        assertThat(removeEvents.get(0).getAutomation()).isEqualTo(automation);
    }

    @Test
    void testRemoveAutomation_notPresent_doesNotThrow() {
        // Given
        Automation automation = createSimpleAutomation("test-automation");

        // When/Then
        assertThatCode(() -> orchestrator.removeAutomation(automation)).doesNotThrowAnyException();
    }

    @Test
    void testRemoveAllAutomations_clearsAllAutomations() {
        // Given
        orchestrator.registerAutomation(createSimpleAutomation("automation-1"));
        orchestrator.registerAutomation(createSimpleAutomation("automation-2"));
        orchestrator.registerAutomation(createSimpleAutomation("automation-3"));
        eventPublisher.clear();

        // When
        orchestrator.removeAllAutomations();

        // Then
        assertThat(orchestrator.getAutomations()).isEmpty();
    }

    @Test
    void testRemoveAllAutomations_publishesRemoveAllEvent() {
        // Given
        Automation automation1 = createSimpleAutomation("automation-1");
        Automation automation2 = createSimpleAutomation("automation-2");
        orchestrator.registerAutomation(automation1);
        orchestrator.registerAutomation(automation2);
        eventPublisher.clear();

        // When
        orchestrator.removeAllAutomations();

        // Then
        List<AutomationEngineRemoveAllEvent> removeAllEvents = eventPublisher
                .getEventsOfType(AutomationEngineRemoveAllEvent.class);
        assertThat(removeAllEvents).hasSize(1);
        assertThat(removeAllEvents.get(0).getAutomations()).containsExactly(automation1, automation2);
    }

    @Test
    void testGetAutomations_returnsDefensiveCopy() {
        // Given
        Automation automation = createSimpleAutomation("test-automation");
        orchestrator.registerAutomation(automation);

        // When
        List<Automation> automations1 = orchestrator.getAutomations();
        List<Automation> automations2 = orchestrator.getAutomations();

        // Then
        assertThat(automations1).isNotSameAs(automations2);
        assertThat(automations1).isEqualTo(automations2);
    }

    @Test
    void testHandleEvent_executesMatchingAutomations() {
        // Given
        SimpleAction action = new SimpleAction("action1");
        Automation automation = new Automation(
                "test",
                null,
                BaseTriggerList.of(new SimpleTrigger("trigger", true)),
                BaseConditionList.of(new SimpleCondition("condition", true)),
                BaseActionList.of(action),
                null);
        orchestrator.registerAutomation(automation);
        TestEvent event = TestEvent.builder().eventType("TEST").build();

        // When
        orchestrator.handleEvent(event);

        // Then
        assertThat(action.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void testHandleEvent_publishesEventAndContext() {
        // Given
        Automation automation = createSimpleAutomation("test");
        orchestrator.registerAutomation(automation);
        eventPublisher.clear();
        TestEvent event = TestEvent.builder().eventType("TEST").build();

        // When
        orchestrator.handleEvent(event);

        // Then
        assertThat(eventPublisher.getPublishedEvents()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(eventPublisher.getEventsOfType(TestEvent.class)).hasSize(1);
        assertThat(eventPublisher.getEventsOfType(EventContext.class)).hasSize(1);
    }

    @Test
    void testHandleEventContext_executesAutomations() {
        // Given
        SimpleAction action = new SimpleAction("action1");
        Automation automation = new Automation(
                "test",
                null,
                BaseTriggerList.of(new SimpleTrigger("trigger", true)),
                BaseConditionList.of(new SimpleCondition("condition", true)),
                BaseActionList.of(action),
                null);
        orchestrator.registerAutomation(automation);
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext eventContext = new EventContext(event);

        // When
        orchestrator.handleEventContext(eventContext);

        // Then
        assertThat(action.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void testHandleEvent_withNullEventContext_throwsException() {
        // When/Then
        assertThatThrownBy(() -> orchestrator.handleEvent((EventContext) null, (a, e) -> {
        }))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EventContext cannot be null");
    }

    @Test
    void testHandleEvent_withEventContextContainingNullEvent_throwsException() {
        // When/Then - EventContext constructor throws when event is null
        assertThatThrownBy(() -> new EventContext(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event cannot be null");
    }

    @Test
    void testExecuteAutomation_whenTriggeredAndConditionsMet_executesActions() {
        // Given
        SimpleAction action = new SimpleAction("action1");
        Automation automation = new Automation(
                "test",
                null,
                BaseTriggerList.of(new SimpleTrigger("trigger", true)),
                BaseConditionList.of(new SimpleCondition("condition", true)),
                BaseActionList.of(action),
                context -> "success");
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext eventContext = new EventContext(event);

        // When
        AutomationResult result = orchestrator.executeAutomation(automation, eventContext);

        // Then
        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getResult().get()).isEqualTo("success");
        assertThat(action.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void testExecuteAutomation_whenNotTriggered_skipsActions() {
        // Given
        SimpleAction action = new SimpleAction("action1");
        Automation automation = new Automation(
                "test",
                null,
                BaseTriggerList.of(new SimpleTrigger("trigger", false)),
                BaseConditionList.of(new SimpleCondition("condition", true)),
                BaseActionList.of(action),
                null);
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext eventContext = new EventContext(event);

        // When
        AutomationResult result = orchestrator.executeAutomation(automation, eventContext);

        // Then
        assertThat(result.isExecuted()).isFalse();
        assertThat(action.getExecutionCount()).isZero();
    }

    @Test
    void testExecuteAutomation_whenConditionsNotMet_skipsActions() {
        // Given
        SimpleAction action = new SimpleAction("action1");
        Automation automation = new Automation(
                "test",
                null,
                BaseTriggerList.of(new SimpleTrigger("trigger", true)),
                BaseConditionList.of(new SimpleCondition("condition", false)),
                BaseActionList.of(action),
                null);
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext eventContext = new EventContext(event);

        // When
        AutomationResult result = orchestrator.executeAutomation(automation, eventContext);

        // Then
        assertThat(result.isExecuted()).isFalse();
        assertThat(action.getExecutionCount()).isZero();
    }

    @Test
    void testExecuteAutomation_publishesProcessedEvent() {
        // Given
        Automation automation = createSimpleAutomation("test");
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext eventContext = new EventContext(event);
        eventPublisher.clear();

        // When
        AutomationResult result = orchestrator.executeAutomation(automation, eventContext);

        // Then
        List<AutomationEngineProcessedEvent> processedEvents = eventPublisher
                .getEventsOfType(AutomationEngineProcessedEvent.class);
        assertThat(processedEvents).hasSize(1);
        // Use alias comparison since Automation creates defensive copies
        assertThat(processedEvents.get(0).getAutomation().getAlias()).isEqualTo(automation.getAlias());
        assertThat(processedEvents.get(0).getEventContext()).isEqualTo(eventContext);
        assertThat(processedEvents.get(0).getResult()).isEqualTo(result);
    }

    @Test
    void testThreadSafety_concurrentRegistration() throws InterruptedException {
        // Given
        int threadCount = 10;
        int automationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger counter = new AtomicInteger(0);

        // When - Concurrently register automations
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < automationsPerThread; j++) {
                        String alias = "automation-" + counter.incrementAndGet();
                        orchestrator.registerAutomation(createSimpleAutomation(alias));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Then
        assertThat(orchestrator.getAutomations()).hasSize(threadCount * automationsPerThread);
    }

    @Test
    void testThreadSafety_concurrentRegistrationAndRemoval() throws InterruptedException {
        // Given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When - Concurrently register and remove automations
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        Automation automation = createSimpleAutomation("automation-" + threadId + "-" + j);
                        orchestrator.registerAutomation(automation);
                        if (j % 2 == 0) {
                            orchestrator.removeAutomation(automation);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Then - Should not throw ConcurrentModificationException
        assertThat(orchestrator.getAutomations()).isNotNull();
        // We expect around 250 automations (50% removed)
        assertThat(orchestrator.getAutomations().size()).isGreaterThan(0);
    }

    @Test
    void testHandleEvent_withCustomExecutionFunction() {
        // Given
        Automation automation = createSimpleAutomation("test");
        orchestrator.registerAutomation(automation);
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext eventContext = new EventContext(event);
        AtomicInteger executionCount = new AtomicInteger(0);

        // When
        orchestrator.handleEvent(eventContext, (a, e) -> {
            executionCount.incrementAndGet();
            assertThat(a).isEqualTo(automation);
            assertThat(e).isEqualTo(eventContext);
        });

        // Then
        assertThat(executionCount.get()).isEqualTo(1);
    }

    @Test
    void testHandleEvent_iteratesOverAllAutomations() {
        // Given
        orchestrator.registerAutomation(createSimpleAutomation("automation-1"));
        orchestrator.registerAutomation(createSimpleAutomation("automation-2"));
        orchestrator.registerAutomation(createSimpleAutomation("automation-3"));
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext eventContext = new EventContext(event);
        AtomicInteger executionCount = new AtomicInteger(0);

        // When
        orchestrator.handleEvent(eventContext, (a, e) -> executionCount.incrementAndGet());

        // Then
        assertThat(executionCount.get()).isEqualTo(3);
    }

    // Helper methods
    private Automation createSimpleAutomation(String alias) {
        return new Automation(
                alias,
                null,
                BaseTriggerList.of(new SimpleTrigger("trigger", false)),
                BaseConditionList.of(new SimpleCondition("condition", true)),
                BaseActionList.of(new SimpleAction("action")),
                null);
    }
}
