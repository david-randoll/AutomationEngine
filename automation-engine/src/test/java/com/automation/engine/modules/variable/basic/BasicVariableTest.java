package com.automation.engine.modules.variable.basic;

import ch.qos.logback.classic.Logger;
import com.automation.engine.AutomationEngineApplication;
import com.automation.engine.TestLogAppender;
import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
import com.automation.engine.modules.events.time_based.TimeBasedEvent;
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
class BasicVariableTest {
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

        engine.removeAll();
    }

    @Test
    void testVariableActionSetsVariableAndLoggerUsesIt() {
        var yaml = """
                alias: Test Variable in Logger
                triggers:
                  - trigger: time
                    at: 14:00
                variables:
                  - someVar: "14:00"
                actions:
                  - action: logger
                    message: "Automation triggered at {{ someVar }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent eventAt14 = new TimeBasedEvent(LocalTime.of(14, 0));
        engine.publishEvent(eventAt14);

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
                variables:
                  - someVar: "15:30"
                actions:
                  - action: logger
                    message: "Automation triggered at {{ someVar | time_format(pattern='hh:mm a') }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent eventAt1530 = new TimeBasedEvent(LocalTime.of(15, 30));
        engine.publishEvent(eventAt1530);

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
                variables:
                  - log_message: "Dynamic log message!"
                actions:
                  - action: logger
                    message: "{{ log_message }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent eventAt1645 = new TimeBasedEvent(LocalTime.of(16, 45));
        engine.publishEvent(eventAt1645);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Dynamic log message!"));
    }
}