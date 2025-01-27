package com.automation.engine.time_based;

import com.automation.engine.AutomationEngineApplication;
import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.factory.AutomationFactory;
import com.automation.engine.modules.time_based.event.TimeBasedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
@ExtendWith(SpringExtension.class)
class TimeBaseTest {
    @Autowired
    private AutomationEngine engine;

    @Autowired
    private AutomationFactory factory;

    @Test
    void basicTest() {
        var yaml = """
                alias: Yaml Automation Test
                triggers:
                  - trigger: timeBasedTrigger
                    at: 22:37
                actions:
                  - action: loggerAction
                    message: Automation triggered at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.addAutomation(automation);

        TimeBasedEvent matchingEvent = new TimeBasedEvent(LocalTime.of(22, 37, 0));
        engine.processEvent(matchingEvent);

        // Assert: Check that the automation triggered as expected
        assertThat(automation.anyTriggerActivated(matchingEvent))
                .as("Automation should trigger when the time matches")
                .isTrue();

        assertThat(automation.allConditionsMet(matchingEvent))
                .as("All conditions should be met for the automation")
                .isTrue();
    }
}
