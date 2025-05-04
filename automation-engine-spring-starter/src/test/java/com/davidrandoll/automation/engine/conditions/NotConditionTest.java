package com.davidrandoll.automation.engine.conditions;

import ch.qos.logback.classic.Logger;
import com.davidrandoll.automation.engine.AutomationEngineApplication;
import com.davidrandoll.automation.engine.TestLogAppender;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.creator.AutomationCreator;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
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
class NotConditionTest {
    @Autowired
    private AutomationEngine engine;

    @Autowired
    private AutomationCreator factory;

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
    void testAutomationWithTimeRangeCondition() {
        var yaml = """
                alias: Not Condition Automation Test
                triggers:
                    - trigger: time
                      at: 22:27
                conditions:
                    - condition: not
                      conditions:
                          - condition: and
                            conditions:
                                - condition: time
                                  after: 22:30
                                - condition: time
                                  before: 23:00
                actions:
                    - action: logger
                      message: Automation triggered when one condition was met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events at different times
        var beforeRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 27)));
        var withinRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 31)));
        var afterRangeEvent = EventContext.of(new TimeBasedEvent(LocalTime.of(23, 5)));

        // Process events
        engine.publishEvent(withinRangeEvent);
        engine.publishEvent(beforeRangeEvent);
        engine.publishEvent(afterRangeEvent);

        // Assert: Automation should trigger for events that meet any condition
        assertThat(automation.anyTriggerActivated(beforeRangeEvent))
                .as("Automation should trigger at 22:27")
                .isTrue();
        assertThat(automation.allConditionsMet(beforeRangeEvent))
                .as("Conditions should be met for 22:27")
                .isTrue();

        assertThat(automation.anyTriggerActivated(withinRangeEvent))
                .as("Automation should not trigger at 22:31 (does not match conditions)")
                .isFalse();
        assertThat(automation.allConditionsMet(withinRangeEvent))
                .as("Conditions should not be met for 22:31")
                .isFalse();

        assertThat(automation.anyTriggerActivated(afterRangeEvent))
                .as("Automation should not trigger after the time range")
                .isFalse();
        assertThat(automation.allConditionsMet(afterRangeEvent))
                .as("Conditions should be met for 23:05")
                .isTrue();

        // Assert: Logged messages
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered when one condition was met at 10:27 PM"))
                .noneMatch(msg -> msg.contains("Automation triggered when one condition was met at 10:31 PM"))
                .noneMatch(msg -> msg.contains("Automation triggered when one condition was met at 11:05 PM"));
    }

    @Test
    void testAutomationWithExactBoundaryTimes() {
        var yaml = """
                alias: Not Condition Boundary Test
                triggers:
                    - trigger: time
                      at: 22:30
                    - trigger: time
                      at: 23:00
                conditions:
                    - condition: not
                      conditions:
                          - condition: and
                            conditions:
                                - condition: time
                                  after: 22:30
                                - condition: time
                                  before: 23:00
                actions:
                    - action: logger
                      message: Automation triggered when one condition was met at {{ time | time_format(pattern='hh:mm a') }}
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create events exactly at boundary times
        var onBoundaryBefore = EventContext.of(new TimeBasedEvent(LocalTime.of(22, 30))); // Should trigger
        var onBoundaryAfter = EventContext.of(new TimeBasedEvent(LocalTime.of(23, 0))); // Should trigger

        // Process events
        engine.publishEvent(onBoundaryBefore);
        engine.publishEvent(onBoundaryAfter);

        // Assert: Automation should trigger when on the boundary
        assertThat(automation.anyTriggerActivated(onBoundaryBefore))
                .as("Automation should trigger at 22:30 as it satisfies 'after 22:30' condition")
                .isTrue();

        assertThat(automation.allConditionsMet(onBoundaryBefore))
                .as("Conditions should be met at 22:30")
                .isTrue();

        assertThat(automation.anyTriggerActivated(onBoundaryAfter))
                .as("Automation should trigger at 23:00 as it satisfies 'before 23:00' condition")
                .isTrue();

        assertThat(automation.allConditionsMet(onBoundaryAfter))
                .as("Conditions should be met at 23:00")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Automation triggered when one condition was met at 10:30 PM"))
                .anyMatch(msg -> msg.contains("Automation triggered when one condition was met at 11:00 PM"));
    }
}