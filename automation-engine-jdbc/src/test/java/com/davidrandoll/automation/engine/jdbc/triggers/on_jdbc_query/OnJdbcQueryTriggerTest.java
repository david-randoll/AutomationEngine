package com.davidrandoll.automation.engine.jdbc.triggers.on_jdbc_query;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Import(OnJdbcQueryTriggerTest.TestConfig.class)
class OnJdbcQueryTriggerTest extends AutomationEngineTest {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public NamedParameterJdbcTemplate jdbcTemplate() {
            return mock(NamedParameterJdbcTemplate.class);
        }
    }

    @Test
    void testAutomationTriggersWhenQueryResultMatchesExpression() {
        var yaml = """
                alias: Trigger when pending tasks exist
                triggers:
                  - trigger: onJdbcQuery
                    query: "SELECT COUNT(*) as count FROM tasks WHERE completed = false"
                    expression: "{{ result[0].count > 0 }}"
                actions:
                  - action: logger
                    message: Pending tasks found
                """;

        // Mock DB result
        Map<String, Object> row = Map.of("count", 2);
        when(jdbcTemplate.queryForList(
                eq("SELECT COUNT(*) as count FROM tasks WHERE completed = false"),
                any(Map.class)
        )).thenReturn(List.of(row));

        // Create and register automation
        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37));
        var context = EventContext.of(event);

        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should trigger when count > 0")
                .isTrue();

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Pending tasks found"));
    }

    @Test
    void testAutomationDoesNotTriggerWhenExpressionFails() {
        var yaml = """
                alias: Should not trigger if no pending tasks
                triggers:
                  - trigger: onJdbcQuery
                    query: SELECT COUNT(*) as count FROM tasks WHERE completed = false
                    expression: "{{ result[0].count > 0 }}"
                actions:
                  - action: logger
                    message: Should not see this
                """;

        // Mock DB result with 0 tasks
        Map<String, Object> row = Map.of("count", 0);
        when(jdbcTemplate.queryForList(
                eq("SELECT COUNT(*) as count FROM tasks WHERE completed = false"),
                any(Map.class)
        )).thenReturn(List.of(row));

        // Create and register automation
        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 0));
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger when count == 0")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not see this"));
    }

    @Test
    void testAutomationHandlesMissingQueryGracefully() {
        var yaml = """
                alias: Missing query
                triggers:
                  - trigger: onJdbcQuery
                    expression: "{{ result[0].count > 0 }}"
                actions:
                  - action: logger
                    message: Should never log
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37));
        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger with missing query")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should never log"));
    }
}
