package com.automation.engine.action_building_block;

import ch.qos.logback.classic.Logger;
import com.automation.engine.AutomationEngineApplication;
import com.automation.engine.TestLogAppender;
import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
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
class StopActionTest {
    @Autowired
    private AutomationEngine engine;

    @Autowired
    private AutomationFactory factory;

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
    void testStopActionSequenceWhenConditionIsMet() {
        var yaml = """
                alias: Stop Sequence When Condition Is Met
                triggers:
                  - trigger: time
                    at: 22:30
                actions:
                  - action: logger
                    message: "Action before stop"
                  - action: stop
                    stopActionSequence: true
                    condition:
                      condition: time
                      after: 22:00
                  - action: logger
                    message: "This should not be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Create event that satisfies stop condition (after 10 PM)
        TimeBasedEvent eventAfter10PM = new TimeBasedEvent(LocalTime.of(22, 30));

        // Process event
        engine.processEvent(eventAfter10PM);

        // Assert: The first log message should be logged, but the second should not
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Action before stop"))
                .noneMatch(msg -> msg.contains("This should not be logged"));
    }

    @Test
    void testContinueActionSequenceWhenConditionIsNotMet() {
        var yaml = """
                alias: Continue Sequence When Condition Is Not Met
                triggers:
                  - trigger: time
                    at: 21:30
                actions:
                  - action: logger
                    message: "Action before stop"
                  - action: stop
                    stopActionSequence: true
                    condition:
                      condition: time
                      after: 22:00
                  - action: logger
                    message: "This should be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Create event that does not satisfy stop condition (before 10 PM)
        TimeBasedEvent eventBefore10PM = new TimeBasedEvent(LocalTime.of(21, 30));

        // Process event
        engine.processEvent(eventBefore10PM);

        // Assert: Both log messages should be logged
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Action before stop"))
                .anyMatch(msg -> msg.contains("This should be logged"));
    }

    @Test
    void testStopActionSequenceWithNoCondition() {
        var yaml = """
                alias: Stop Action Sequence With No Condition
                triggers:
                  - trigger: time
                    at: 20:00
                actions:
                  - action: logger
                    message: "Action before stop"
                  - action: stop
                  - action: logger
                    message: "This should be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Create an event with no condition set
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(20, 0));

        // Process event
        engine.processEvent(event);

        // Assert: Stop action has no condition, so execution should continue
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Action before stop"))
                .anyMatch(msg -> msg.contains("This should be logged"));
    }

    @Test
    void testStopActionSequenceInMiddle() {
        var yaml = """
                alias: Stop Action Sequence In Middle
                triggers:
                  - trigger: time
                    at: 12:00
                actions:
                  - action: logger
                    message: "Start sequence"
                  - action: stop
                    stopActionSequence: true
                    condition:
                      condition: alwaysTrue
                  - action: logger
                    message: "This should not be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Create a time event at 12:00
        TimeBasedEvent eventAtNoon = new TimeBasedEvent(LocalTime.of(12, 0));

        // Process event
        engine.processEvent(eventAtNoon);

        // Assert: The first log message should be logged, but the second should not
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Start sequence"))
                .noneMatch(msg -> msg.contains("This should not be logged"));
    }


    @Test
    void testStopActionSequenceAsFirstAction() {
        var yaml = """
                alias: Stop Action Sequence As First Action
                triggers:
                  - trigger: time
                    at: 08:00
                actions:
                  - action: stop
                    stopActionSequence: true
                    condition:
                      condition: alwaysTrue
                  - action: logger
                    message: "This should not be logged"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Create a time event at 08:00
        TimeBasedEvent eventAt8AM = new TimeBasedEvent(LocalTime.of(8, 0));

        // Process event
        engine.processEvent(eventAt8AM);

        // Assert: No actions should be logged since the sequence is stopped immediately
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not be logged"));
    }
}
