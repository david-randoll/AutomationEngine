package com.davidrandoll.automation.engine.actions;

import com.davidrandoll.automation.engine.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.modules.actions.parallel.ParallelAction;
import com.davidrandoll.automation.engine.modules.actions.wait_for_trigger.WaitForTriggerAction;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.provider.AEConfigProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ParallelActionTest extends AutomationEngineTest {

    @Autowired
    private ParallelAction parallelAction;

    @Test
    void testParallelActionsExecuteIndependently() {
        var yaml = """
                alias: Parallel Actions Test
                triggers:
                  - trigger: time
                    at: 15:00
                actions:
                  - action: logger
                    message: "Before parallel execution"
                  - action: parallel
                    actions:
                      - action: logger
                        message: "Parallel action 1"
                      - action: logger
                        message: "Parallel action 2"
                  - action: logger
                    message: "After parallel execution"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt3PM = new TimeBasedEvent(LocalTime.of(15, 0));
        engine.publishEvent(eventAt3PM);

        // Assert: Ensure all actions were logged
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Before parallel execution"))
                .anyMatch(msg -> msg.contains("Parallel action 1"))
                .anyMatch(msg -> msg.contains("Parallel action 2"))
                .anyMatch(msg -> msg.contains("After parallel execution"));
    }

    @Test
    void testParallelExecutionWithExecutorProvider() {
        // Mock executor provider to check if it was used
        AEConfigProvider mockAutomationEngineConfigurationProvider = mock(AEConfigProvider.class);
        when(mockAutomationEngineConfigurationProvider.getExecutor()).thenReturn(Executors.newFixedThreadPool(2));

        // Inject mock provider
        ReflectionTestUtils.setField(parallelAction, WaitForTriggerAction.Fields.provider, mockAutomationEngineConfigurationProvider);

        var yaml = """
                alias: Parallel Execution With ExecutorProvider
                triggers:
                  - trigger: time
                    at: 16:00
                actions:
                  - action: logger
                    message: "Before parallel execution"
                  - action: parallel
                    actions:
                      - action: logger
                        message: "Parallel action 1"
                      - action: logger
                        message: "Parallel action 2"
                  - action: logger
                    message: "After parallel execution"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt4PM = new TimeBasedEvent(LocalTime.of(16, 0));
        engine.publishEvent(eventAt4PM);

        // Assert: Ensure parallel execution was used with the provided executor
        verify(mockAutomationEngineConfigurationProvider, times(1)).getExecutor();
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Before parallel execution"))
                .anyMatch(msg -> msg.contains("Parallel action 1"))
                .anyMatch(msg -> msg.contains("Parallel action 2"))
                .anyMatch(msg -> msg.contains("After parallel execution"));
    }

    @Test
    void testParallelExecutionWithoutExecutorProvider() {
        var yaml = """
                alias: Parallel Execution Without ExecutorProvider
                triggers:
                  - trigger: time
                    at: 17:00
                actions:
                  - action: logger
                    message: "Before parallel execution"
                  - action: parallel
                    actions:
                      - action: logger
                        message: "Parallel action 1"
                      - action: logger
                        message: "Parallel action 2"
                  - action: logger
                    message: "After parallel execution"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt5PM = new TimeBasedEvent(LocalTime.of(17, 0));
        engine.publishEvent(eventAt5PM);

        // Assert: Ensure all actions executed and default executor was used
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Before parallel execution"))
                .anyMatch(msg -> msg.contains("Parallel action 1"))
                .anyMatch(msg -> msg.contains("Parallel action 2"))
                .anyMatch(msg -> msg.contains("After parallel execution"));
    }

    @Test
    void testParallelExecutionDoesNotBlockMainThread() {
        var yaml = """
                alias: Non-Blocking Parallel Execution
                triggers:
                  - trigger: time
                    at: 18:00
                actions:
                  - action: logger
                    message: "Before parallel execution"
                  - action: parallel
                    actions:
                      - action: delay
                        duration: PT2S
                      - action: delay
                        duration: PT1S
                      - action: logger
                        message: "Parallel action completed"
                  - action: logger
                    message: "After parallel execution"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt6PM = new TimeBasedEvent(LocalTime.of(18, 0));
        long startTime = System.currentTimeMillis();
        engine.publishEvent(eventAt6PM);
        long endTime = System.currentTimeMillis();

        assertThat(endTime - startTime).isLessThan(2500);

        // Assert: Ensure main actions execute immediately while parallel actions complete later
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Before parallel execution"))
                .anyMatch(msg -> msg.contains("After parallel execution")); // Should be logged immediately
    }

}
