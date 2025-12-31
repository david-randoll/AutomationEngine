package com.davidrandoll.automation.engine.spring.modules.events.time_based;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
        logAppender.clear();

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
        logAppender.clear();

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
        logAppender.clear();

        // 11:59:00 PM (No fire)
        LocalDateTime before1 = LocalDateTime.of(2025, 12, 29, 23, 59, 0);
        engine.publishEvent(new TimeBasedEvent(before1.toLocalTime(), before1));

        // 11:59:59 PM (No fire)
        LocalDateTime before2 = LocalDateTime.of(2025, 12, 29, 23, 59, 59);
        engine.publishEvent(new TimeBasedEvent(before2.toLocalTime(), before2));

        // 12:00:00 AM (Fire)
        LocalDateTime midnight = LocalDateTime.of(2025, 12, 30, 0, 0, 0);
        engine.publishEvent(new TimeBasedEvent(midnight.toLocalTime(), midnight));

        // 12:00:40 AM (No fire)
        LocalDateTime after1 = LocalDateTime.of(2025, 12, 30, 0, 0, 40);
        engine.publishEvent(new TimeBasedEvent(after1.toLocalTime(), after1));

        // 12:01:00 AM (No fire)
        LocalDateTime after2 = LocalDateTime.of(2025, 12, 30, 0, 1, 0);
        engine.publishEvent(new TimeBasedEvent(after2.toLocalTime(), after2));

        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Midnight reached")).count()).isEqualTo(1);
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
        logAppender.clear();

        // 10:00:00 (Yes)
        LocalDateTime dt0 = LocalDateTime.of(2025, 12, 30, 10, 0, 0);
        engine.publishEvent(new TimeBasedEvent(dt0.toLocalTime(), dt0));

        // 10:00:01 (No)
        LocalDateTime dt1 = LocalDateTime.of(2025, 12, 30, 10, 0, 1);
        engine.publishEvent(new TimeBasedEvent(dt1.toLocalTime(), dt1));

        // 10:00:29 (No)
        LocalDateTime dt29 = LocalDateTime.of(2025, 12, 30, 10, 0, 29);
        engine.publishEvent(new TimeBasedEvent(dt29.toLocalTime(), dt29));

        // 10:00:30 (Yes)
        LocalDateTime dt30 = LocalDateTime.of(2025, 12, 30, 10, 0, 30);
        engine.publishEvent(new TimeBasedEvent(dt30.toLocalTime(), dt30));

        // 10:00:31 (No)
        LocalDateTime dt31 = LocalDateTime.of(2025, 12, 30, 10, 0, 31);
        engine.publishEvent(new TimeBasedEvent(dt31.toLocalTime(), dt31));

        // 10:00:59 (No)
        LocalDateTime dt59 = LocalDateTime.of(2025, 12, 30, 10, 0, 59);
        engine.publishEvent(new TimeBasedEvent(dt59.toLocalTime(), dt59));

        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.startsWith("Tick at")).count()).isEqualTo(2);
        assertThat(logAppender.getLoggedMessages()).contains("Tick at 10:00:00");
        assertThat(logAppender.getLoggedMessages()).contains("Tick at 10:00:30");
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
        logAppender.clear();

        // Sunday, Dec 28, 2025 at 9:00:00 AM (No)
        LocalDateTime sunday = LocalDateTime.of(2025, 12, 28, 9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(sunday.toLocalTime(), sunday));

        // Monday, Dec 29, 2025 at 8:59:59 AM (No)
        LocalDateTime mondayBefore = LocalDateTime.of(2025, 12, 29, 8, 59, 59);
        engine.publishEvent(new TimeBasedEvent(mondayBefore.toLocalTime(), mondayBefore));

        // Monday, Dec 29, 2025 at 9:00:00 AM (Yes)
        LocalDateTime monday = LocalDateTime.of(2025, 12, 29, 9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(monday.toLocalTime(), monday));

        // Monday, Dec 29, 2025 at 9:00:01 AM (No)
        LocalDateTime mondayAfter = LocalDateTime.of(2025, 12, 29, 9, 0, 1);
        engine.publishEvent(new TimeBasedEvent(mondayAfter.toLocalTime(), mondayAfter));

        // Tuesday, Dec 30, 2025 at 9:00:00 AM (No)
        LocalDateTime tuesday = LocalDateTime.of(2025, 12, 30, 9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(tuesday.toLocalTime(), tuesday));

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
        logAppender.clear();

        // Feb 28, 2024 at 12:00:00 PM (No)
        LocalDateTime feb28 = LocalDateTime.of(2024, 2, 28, 12, 0, 0);
        engine.publishEvent(new TimeBasedEvent(feb28.toLocalTime(), feb28));

        // Feb 29, 2024 at 11:59:59 AM (No)
        LocalDateTime leapDayBefore = LocalDateTime.of(2024, 2, 29, 11, 59, 59);
        engine.publishEvent(new TimeBasedEvent(leapDayBefore.toLocalTime(), leapDayBefore));

        // Feb 29, 2024 at 12:00:00 PM (Yes)
        LocalDateTime leapDay = LocalDateTime.of(2024, 2, 29, 12, 0, 0);
        engine.publishEvent(new TimeBasedEvent(leapDay.toLocalTime(), leapDay));

        // Feb 29, 2024 at 12:00:01 PM (No)
        LocalDateTime leapDayAfter = LocalDateTime.of(2024, 2, 29, 12, 0, 1);
        engine.publishEvent(new TimeBasedEvent(leapDayAfter.toLocalTime(), leapDayAfter));

        // March 1, 2024 at 12:00:00 PM (No)
        LocalDateTime march1 = LocalDateTime.of(2024, 3, 1, 12, 0, 0);
        engine.publishEvent(new TimeBasedEvent(march1.toLocalTime(), march1));

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
        logAppender.clear();

        // Oct 30, 2025 at 11:59:59 PM (No)
        LocalDateTime oct30 = LocalDateTime.of(2025, 10, 30, 23, 59, 59);
        engine.publishEvent(new TimeBasedEvent(oct30.toLocalTime(), oct30));

        // Oct 31, 2025 at 12:00:00 AM (Yes)
        LocalDateTime oct31 = LocalDateTime.of(2025, 10, 31, 0, 0, 0);
        engine.publishEvent(new TimeBasedEvent(oct31.toLocalTime(), oct31));

        // Oct 31, 2025 at 12:00:01 AM (No)
        LocalDateTime oct31After = LocalDateTime.of(2025, 10, 31, 0, 0, 1);
        engine.publishEvent(new TimeBasedEvent(oct31After.toLocalTime(), oct31After));

        // Feb 28, 2025 at 12:00:00 AM (Yes - 2025 is not a leap year)
        LocalDateTime feb28 = LocalDateTime.of(2025, 2, 28, 0, 0, 0);
        engine.publishEvent(new TimeBasedEvent(feb28.toLocalTime(), feb28));

        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Month End")).count()).isEqualTo(2);
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
        logAppender.clear();

        // Monday at 8:59:59 AM (No)
        LocalDateTime mondayBefore = LocalDateTime.of(2025, 12, 29, 8, 59, 59);
        engine.publishEvent(new TimeBasedEvent(mondayBefore.toLocalTime(), mondayBefore));

        // Monday at 9:00:00 AM (Yes)
        LocalDateTime monday9am = LocalDateTime.of(2025, 12, 29, 9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(monday9am.toLocalTime(), monday9am));

        // Monday at 5:00:00 PM (Yes - 17:00)
        LocalDateTime monday5pm = LocalDateTime.of(2025, 12, 29, 17, 0, 0);
        engine.publishEvent(new TimeBasedEvent(monday5pm.toLocalTime(), monday5pm));

        // Monday at 5:00:01 PM (No)
        LocalDateTime mondayAfter1 = LocalDateTime.of(2025, 12, 29, 17, 0, 1);
        engine.publishEvent(new TimeBasedEvent(mondayAfter1.toLocalTime(), mondayAfter1));

        // Monday at 5:01:00 PM (No)
        LocalDateTime mondayAfter2 = LocalDateTime.of(2025, 12, 29, 17, 1, 0);
        engine.publishEvent(new TimeBasedEvent(mondayAfter2.toLocalTime(), mondayAfter2));

        // Saturday at 9:00:00 AM (No)
        LocalDateTime saturday9am = LocalDateTime.of(2026, 1, 3, 9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(saturday9am.toLocalTime(), saturday9am));

        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Working...")).count()).isEqualTo(2);
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
        logAppender.clear();

        LocalDateTime dt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        engine.publishEvent(new TimeBasedEvent(dt.toLocalTime(), dt));
        engine.publishEvent(new TimeBasedEvent(dt.plusSeconds(1).toLocalTime(), dt.plusSeconds(1)));

        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("Second Tick")).count()).isEqualTo(2);
    }

    @Test
    void testAtTrigger() {
        var yaml = """
                alias: At Trigger Test
                triggers:
                  - trigger: time
                    at: "09:00:00"
                actions:
                  - action: logger
                    message: At 9am
                """;

        engine.register(factory.createAutomation("yaml", yaml));
        logAppender.clear();

        // 8:59:59 AM (No)
        LocalTime before = LocalTime.of(8, 59, 59);
        engine.publishEvent(new TimeBasedEvent(before, LocalDateTime.of(2025, 12, 30, 8, 59, 59)));

        // 9:00:00 AM (Yes)
        LocalTime at = LocalTime.of(9, 0, 0);
        engine.publishEvent(new TimeBasedEvent(at, LocalDateTime.of(2025, 12, 30, 9, 0, 0)));

        // 9:00:01 AM (No)
        LocalTime after = LocalTime.of(9, 0, 1);
        engine.publishEvent(new TimeBasedEvent(after, LocalDateTime.of(2025, 12, 30, 9, 0, 1)));

        assertThat(logAppender.getLoggedMessages().stream().filter(m -> m.equals("At 9am")).count()).isEqualTo(1);
    }
}
