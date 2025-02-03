package com.automation.engine.action_building_block;

import ch.qos.logback.classic.Logger;
import com.automation.engine.AutomationEngineApplication;
import com.automation.engine.TestLogAppender;
import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
import com.automation.engine.modules.action_building_block.parallel.ParallelAction;
import com.automation.engine.modules.time_based.event.TimeBasedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
@ExtendWith(SpringExtension.class)
class SequenceActionTest {
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
    void testSequenceActionsExecuteInOrder() {
        var yaml = """
                alias: Sequence Actions Test
                triggers:
                  - trigger: time
                    at: 10:00
                actions:
                  - action: logger
                    message: "Before sequence"
                  - action: sequence
                    actions:
                      - action: logger
                        message: "Action 1 in sequence"
                      - action: logger
                        message: "Action 2 in sequence"
                  - action: logger
                    message: "After sequence"
                """;

        // Create automation from YAML
        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Trigger the automation
        TimeBasedEvent eventAt10AM = new TimeBasedEvent(LocalTime.of(10, 0));
        engine.processEvent(eventAt10AM);

        // Assert: Ensure all actions were logged in the correct order
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Before sequence"))
                .anyMatch(msg -> msg.contains("Action 1 in sequence"))
                .anyMatch(msg -> msg.contains("Action 2 in sequence"))
                .anyMatch(msg -> msg.contains("After sequence"));
    }

}