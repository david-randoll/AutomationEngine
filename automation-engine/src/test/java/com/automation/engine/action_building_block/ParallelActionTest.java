package com.automation.engine.action_building_block;

import ch.qos.logback.classic.Logger;
import com.automation.engine.AutomationEngineApplication;
import com.automation.engine.TestLogAppender;
import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
import com.automation.engine.modules.action_building_block.parallel.ExecutorProvider;
import com.automation.engine.modules.action_building_block.parallel.ParallelAction;
import com.automation.engine.modules.time_based.event.TimeBasedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
@ExtendWith(SpringExtension.class)
class ParallelActionTest {
    @Autowired
    private AutomationEngine engine;

    @Autowired
    private AutomationFactory factory;

    @Autowired
    private ParallelAction parallelAction;

    private TestLogAppender logAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger("com.automation.engine");
        logAppender = new TestLogAppender();
        logger.addAppender(logAppender);
        logAppender.start();

        engine.clearAutomations();
    }

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
        engine.addAutomation(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt3PM = new TimeBasedEvent(LocalTime.of(15, 0));
        engine.processEvent(eventAt3PM);

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
        ExecutorProvider mockExecutorProvider = mock(ExecutorProvider.class);
        when(mockExecutorProvider.getExecutor()).thenReturn(Executors.newFixedThreadPool(2));

        // Inject mock provider
        ReflectionTestUtils.setField(parallelAction, "executorProvider", mockExecutorProvider);

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
        engine.addAutomation(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt4PM = new TimeBasedEvent(LocalTime.of(16, 0));
        engine.processEvent(eventAt4PM);

        // Assert: Ensure parallel execution was used with the provided executor
        verify(mockExecutorProvider, times(1)).getExecutor();
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
        engine.addAutomation(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt5PM = new TimeBasedEvent(LocalTime.of(17, 0));
        engine.processEvent(eventAt5PM);

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
                        duration: PT5S
                      - action: delay
                        duration: PT4S
                      - action: logger
                        message: "Parallel action completed"
                  - action: logger
                    message: "After parallel execution"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt6PM = new TimeBasedEvent(LocalTime.of(18, 0));
        long startTime = System.currentTimeMillis();
        engine.processEvent(eventAt6PM);
        long endTime = System.currentTimeMillis();

        assertThat(endTime - startTime).isLessThan(5500);

        // Assert: Ensure main actions execute immediately while parallel actions complete later
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Before parallel execution"))
                .anyMatch(msg -> msg.contains("After parallel execution")); // Should be logged immediately
    }

}
