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
class VariableActionTest {

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
    void testVariableActionSetsVariableAndLoggerUsesIt() {
        var yaml = """
                alias: Test Variable in Logger
                triggers:
                  - trigger: time
                    at: 14:00
                actions:
                  - action: variable
                    someVar: "14:00"
                  - action: logger
                    message: "Automation triggered at {{ someVar }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Trigger automation at 14:00
        TimeBasedEvent eventAt14 = new TimeBasedEvent(LocalTime.of(14, 0));
        engine.processEvent(eventAt14);

        // Assert: Ensure the log contains the resolved message
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered at 14:00"));
    }

    @Test
    void testVariableActionWithTimeFormatting() {
        var yaml = """
                alias: Test Variable with Time Formatting
                triggers:
                  - trigger: time
                    at: 15:30
                actions:
                  - action: variable
                    someVar: "15:30"
                  - action: logger
                    message: "Automation triggered at {{ someVar | time_format(pattern='hh:mm a') }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Trigger automation at 15:30
        TimeBasedEvent eventAt1530 = new TimeBasedEvent(LocalTime.of(15, 30));
        engine.processEvent(eventAt1530);

        // Assert: Ensure the log contains the formatted time
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered at 03:30 PM"));
    }

    @Test
    void testLoggerUsesMessageSetInVariable() {
        var yaml = """
                alias: Test Logger Uses Variable Message
                triggers:
                  - trigger: time
                    at: 16:45
                actions:
                  - action: variable
                    log_message: "Dynamic log message!"
                  - action: logger
                    message: "{{ log_message }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Trigger automation at 16:45
        TimeBasedEvent eventAt1645 = new TimeBasedEvent(LocalTime.of(16, 45));
        engine.processEvent(eventAt1645);

        // Assert: Ensure the logger correctly logs the message stored in the variable
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Dynamic log message!"));
    }

    @Test
    void testConditionalVariableAssignmentAndLogging() {
        var yaml = """
                alias: Test Conditional Variable Assignment
                triggers:
                  - trigger: time
                    at: 18:00
                actions:
                  - action: ifThenElse
                    if:
                      - condition: time
                        before: 18:30
                    then:
                      - action: variable
                        status_message: "Its still early!"
                    else:
                      - action: variable
                        status_message: "Its getting late!"
                  - action: logger
                    message: "{{ status_message }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        // Act: Trigger automation at 18:00 (before 18:30, so 'then' branch should execute)
        TimeBasedEvent eventAt18 = new TimeBasedEvent(LocalTime.of(18, 0));
        engine.processEvent(eventAt18);

        // Assert: Ensure the logger outputs the correct message based on the condition
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Its still early!"))
                .noneMatch(msg -> msg.contains("Its getting late!"));
    }


}
