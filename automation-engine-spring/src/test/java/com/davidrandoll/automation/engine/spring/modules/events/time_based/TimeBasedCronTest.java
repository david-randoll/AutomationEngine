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

    @Test
    void testMidnightCron() {
        var yaml = """
                alias: Midnight Test
                triggers:
                  - trigger: time
                    cron: "0 0 0 * * *"
                actions:
                  - action: logger
                    message: Midnight reached
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        LocalDateTime midnight = LocalDateTime.of(2025, 12, 30, 0, 0, 0);
        engine.publishEvent(new TimeBasedEvent(midnight.toLocalTime(), midnight));

        assertThat(logAppender.getLoggedMessages()).contains("Midnight reached");
    }

    @Test
    void testEvery30SecondsCron() {
        var yaml = """
                alias: 30s Test
                triggers:
                  - trigger: time
                    cron: "*/30 * * * * *"
                actions:
                  - action: logger
                    message: Tick at {{ time }}
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        // 0 seconds
        LocalDateTime dt0 = LocalDateTime.of(2025, 12, 30, 10, 0, 0);
        engine.publishEvent(new TimeBasedEvent(dt0.toLocalTime(), dt0));
        assertThat(logAppender.getLoggedMessages()).contains("Tick at 10:00:00");

        // 30 seconds
        LocalDateTime dt30 = LocalDateTime.of(2025, 12, 30, 10, 0, 30);
        engine.publishEvent(new TimeBasedEvent(dt30.toLocalTime(), dt30));
        assertThat(logAppender.getLoggedMessages()).contains("Tick at 10:00:30");

        // 15 seconds (should not trigger)
        LocalDateTime dt15 = LocalDateTime.of(2025, 12, 30, 10, 0, 15);
        engine.publishEvent(new TimeBasedEvent(dt15.toLocalTime(), dt15));
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Tick at 10:00:15"));
    }

    @Test
    void testWeeklyCron() {
        var yaml = """
                alias: Weekly Test
                triggers:
                  - trigger: time
                    cron: "0 0 9 * * MON"
                actions:
                  - action: logger
                    message: Monday Morning
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        // Monday, Dec 29, 2025
        LocalDateTime monday = LocalDateTime.of(2025, 12, 29, 9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(monday.toLocalTime(), monday));
        assertThat(logAppender.getLoggedMessages()).contains("Monday Morning");

        // Tuesday, Dec 30, 2025
        LocalDateTime tuesday = LocalDateTime.of(2025, 12, 30, 9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(tuesday.toLocalTime(), tuesday));
        // Should still only have 1 "Monday Morning" from the first event
        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Monday Morning")).count()).isEqualTo(1);
    }

    @Test
    void testLeapDayCron() {
        var yaml = """
                alias: Leap Day Test
                triggers:
                  - trigger: time
                    cron: "0 0 12 29 2 *"
                actions:
                  - action: logger
                    message: Leap Day Noon
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        // Feb 29, 2024 (Leap Year)
        LocalDateTime leapDay = LocalDateTime.of(2024, 2, 29, 12, 0, 0);
        engine.publishEvent(new TimeBasedEvent(leapDay.toLocalTime(), leapDay));
        assertThat(logAppender.getLoggedMessages()).contains("Leap Day Noon");

        // Feb 28, 2024
        LocalDateTime feb28 = LocalDateTime.of(2024, 2, 28, 12, 0, 0);
        engine.publishEvent(new TimeBasedEvent(feb28.toLocalTime(), feb28));
        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Leap Day Noon")).count()).isEqualTo(1);
    }

    @Test
    void testLastDayOfMonthCron() {
        var yaml = """
                alias: Last Day Test
                triggers:
                  - trigger: time
                    cron: "0 0 0 L * *"
                actions:
                  - action: logger
                    message: Month End
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        // Oct 31st
        LocalDateTime oct31 = LocalDateTime.of(2025, 10, 31, 0, 0, 0);
        engine.publishEvent(new TimeBasedEvent(oct31.toLocalTime(), oct31));
        assertThat(logAppender.getLoggedMessages()).contains("Month End");

        // Oct 30th
        LocalDateTime oct30 = LocalDateTime.of(2025, 10, 30, 0, 0, 0);
        engine.publishEvent(new TimeBasedEvent(oct30.toLocalTime(), oct30));
        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Month End")).count()).isEqualTo(1);
    }

    @Test
    void testBusinessHoursCron() {
        var yaml = """
                alias: Business Hours Test
                triggers:
                  - trigger: time
                    cron: "0 0 9-17 * * MON-FRI"
                actions:
                  - action: logger
                    message: Working...
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        // Monday at 9 AM
        LocalDateTime monday9am = LocalDateTime.of(2025, 12, 29, 9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(monday9am.toLocalTime(), monday9am));
        assertThat(logAppender.getLoggedMessages()).contains("Working...");

        // Monday at 6 PM
        LocalDateTime monday6pm = LocalDateTime.of(2025, 12, 29, 18, 0, 0);
        engine.publishEvent(new TimeBasedEvent(monday6pm.toLocalTime(), monday6pm));
        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Working...")).count()).isEqualTo(1);

        // Saturday at 9 AM
        LocalDateTime saturday9am = LocalDateTime.of(2026, 1, 3, 9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(saturday9am.toLocalTime(), saturday9am));
        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Working...")).count()).isEqualTo(1);
    }

    @Test
    void testEverySecondCron() {
        var yaml = """
                alias: Every Second Test
                triggers:
                  - trigger: time
                    cron: "* * * 1 1 *"
                actions:
                  - action: logger
                    message: Second Tick
                """;

        engine.register(factory.createAutomation("yaml", yaml));

        LocalDateTime dt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        engine.publishEvent(new TimeBasedEvent(dt.toLocalTime(), dt));
        engine.publishEvent(new TimeBasedEvent(dt.plusSeconds(1).toLocalTime(), dt.plusSeconds(1)));

        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Second Tick")).count()).isEqualTo(2);
    }
}
