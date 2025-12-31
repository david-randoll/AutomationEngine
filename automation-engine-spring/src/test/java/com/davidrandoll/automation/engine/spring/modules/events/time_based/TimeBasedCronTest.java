package com.davidrandoll.automation.engine.spring.modules.events.time_based;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TimeBasedCronTest extends AutomationEngineTest {

    @Test
    void testThatAutomationTriggersWithCronExpression() {
        var yaml = """
                alias: Cron Automation Test
                triggers:
                  - trigger: time
                    cron: "0 0 9 * * *"
                actions:
                  - action: logger
                    message: Automation triggered by cron at {{ time }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // 9:00 AM matches "0 0 9 * * *"
        LocalDateTime matchDateTime = LocalDateTime.of(2025, 12, 29, 9, 0, 0);
        var matchingEvent = new TimeBasedEvent(matchDateTime.toLocalTime(), matchDateTime);
        
        engine.publishEvent(matchingEvent);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered by cron at 09:00"));
    }

    @Test
    void testAutomationDoesNotTriggerForNonMatchingCron() {
        var yaml = """
                alias: Non-Matching Cron Test
                triggers:
                  - trigger: time
                    cron: "0 0 9 * * *"
                actions:
                  - action: logger
                    message: Automation triggered by cron at {{ time }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // 10:00 AM does not match "0 0 9 * * *"
        LocalDateTime nonMatchDateTime = LocalDateTime.of(2025, 12, 29, 10, 0, 0);
        var nonMatchingEvent = new TimeBasedEvent(nonMatchDateTime.toLocalTime(), nonMatchDateTime);

        engine.publishEvent(nonMatchingEvent);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Automation triggered by cron at 10:00"));
    }
}
