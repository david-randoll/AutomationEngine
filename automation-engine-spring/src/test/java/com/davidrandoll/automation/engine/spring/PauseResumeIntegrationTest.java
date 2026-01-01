package com.davidrandoll.automation.engine.spring;

import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.events.publisher.AutomationEngineProcessedEvent;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.orchestrator.IAEOrchestrator;
import com.davidrandoll.automation.engine.test.EventCaptureListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = {AESpringConfig.class, PauseResumeIntegrationTest.TestConfig.class})
@ActiveProfiles("test")
public class PauseResumeIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }

        @Bean
        public TaskScheduler taskScheduler() {
            return new ThreadPoolTaskScheduler();
        }

        @Bean
        public EventCaptureListener eventCaptureListener() {
            return new EventCaptureListener();
        }

        @Bean("always_trueTrigger")
        public ITrigger alwaysTrueTrigger() {
            return (ec, tc) -> true;
        }

        @Bean("logAction")
        public IAction logAction() {
            return (ec, ac) -> {
                return ActionResult.CONTINUE;
            };
        }
    }

    @Autowired
    private IAEOrchestrator orchestrator;

    @Autowired
    private com.davidrandoll.automation.engine.creator.AutomationFactory automationFactory;

    @Autowired
    private EventCaptureListener eventCaptureListener;

    @Test
    void testPauseAndResumeWithDelay() {
        String yaml = """
                alias: pause-resume-test
                triggers:
                  - trigger: always_true
                actions:
                  - action: logger
                    message: "Before delay"
                  - action: delay
                    duration: PT1S
                  - action: logger
                    message: "After delay"
                """;

        com.davidrandoll.automation.engine.core.Automation automation = automationFactory.createAutomation("yaml", yaml);
        orchestrator.registerAutomation(automation);
        eventCaptureListener.clearEvents();

        IEvent event = new IEvent() {};

        orchestrator.handleEvent(event);

        // Wait for the first processed event (PAUSE)
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Object> events = eventCaptureListener.getEvents();
            boolean hasPausedEvent = events.stream()
                    .filter(e -> e instanceof AutomationEngineProcessedEvent)
                    .map(e -> (AutomationEngineProcessedEvent) e)
                    .anyMatch(e -> e.getResult().isPaused());
            assertThat(hasPausedEvent).isTrue();
        });

        // Wait for resumption and completion
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Object> events = eventCaptureListener.getEvents();
            long processedEvents = events.stream()
                    .filter(e -> e instanceof AutomationEngineProcessedEvent)
                    .count();
            
            // Should have 2 processed events: one for PAUSE, one for CONTINUE/STOP
            assertThat(processedEvents).isEqualTo(2);
            
            AutomationEngineProcessedEvent lastEvent = (AutomationEngineProcessedEvent) events.stream()
                    .filter(e -> e instanceof AutomationEngineProcessedEvent)
                    .reduce((first, second) -> second)
                    .orElseThrow();
            
            assertThat(lastEvent.getResult().isExecuted()).isTrue();
            assertThat(lastEvent.getResult().isPaused()).isFalse();
        });
    }
}
