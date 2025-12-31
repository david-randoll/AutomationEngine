package com.davidrandoll.automation.engine.spring.jdbc.triggers.on_query;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.jdbc.TestConfig;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@Import(TestConfig.class)
class OnJdbcQueryTriggerTest extends AutomationEngineTest {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetMocks() {
        reset(jdbcTemplate);
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
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37));
        var context = EventContext.of(event);

        engine.executeAutomation(automation, context);

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
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 0));
        var context = EventContext.of(event);
        engine.executeAutomation(automation, context);

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
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37));
        var context = EventContext.of(event);
        engine.executeAutomation(automation, context);

        assertThat(automation.anyTriggerActivated(context))
                .as("Should not trigger with missing query")
                .isFalse();

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should never log"));
    }

    @Test
    void testTriggerSkips_whenExpressionIsMissing() {
        var yaml = """
                alias: Missing expression
                triggers:
                  - trigger: onJdbcQuery
                    query: SELECT COUNT(*) as count FROM tasks
                actions:
                  - action: logger
                    message: Should not log
                """;

        Map<String, Object> row = Map.of("count", 2);
        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenReturn(List.of(row));

        Automation automation = factory.createAutomation("yaml", yaml);
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37));
        var context = EventContext.of(event);
        engine.executeAutomation(automation, context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not log"));
    }

    @Test
    void testTriggerSkips_whenQueryReturnsEmptyResult() {
        var yaml = """
                alias: Empty result test
                triggers:
                  - trigger: onJdbcQuery
                    query: SELECT * FROM tasks
                    expression: "{{ result.size > 0 }}"
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenReturn(List.of());

        Automation automation = factory.createAutomation("yaml", yaml);
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(22, 37));
        var context = EventContext.of(event);

        engine.executeAutomation(automation, context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void shouldTrigger_whenQueryResultMatchesExpression() {
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

        when(jdbcTemplate.queryForList(
                eq("SELECT COUNT(*) as count FROM tasks WHERE completed = false"),
                any(Map.class)
        )).thenReturn(List.of(Map.of("count", 2)));

        Automation automation = factory.createAutomation("yaml", yaml);

        EventContext context = EventContext.of(new TimeBasedEvent(LocalTime.of(12, 0)));
        engine.executeAutomation(automation, context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Pending tasks found"));
    }

    @Test
    void shouldNotTrigger_whenExpressionEvaluatesToFalse() {
        var yaml = """
                alias: No trigger for empty result
                triggers:
                  - trigger: onJdbcQuery
                    query: SELECT COUNT(*) as count FROM tasks WHERE completed = false
                    expression: "{{ result[0].count > 0 }}"
                actions:
                  - action: logger
                    message: Should not see this
                """;

        when(jdbcTemplate.queryForList(
                eq("SELECT COUNT(*) as count FROM tasks WHERE completed = false"),
                any(Map.class)
        )).thenReturn(List.of(Map.of("count", 0)));

        Automation automation = factory.createAutomation("yaml", yaml);

        EventContext context = EventContext.of(new TimeBasedEvent(LocalTime.of(9, 30)));
        engine.executeAutomation(automation, context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not see this"));
    }

    @Test
    void shouldNotTrigger_whenQueryIsMissing() {
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

        EventContext context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 45)));
        engine.executeAutomation(automation, context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should never log"));
    }

    @Test
    void shouldNotTrigger_whenExpressionIsMissing() {
        var yaml = """
                alias: Missing expression
                triggers:
                  - trigger: onJdbcQuery
                    query: SELECT * FROM tasks
                actions:
                  - action: logger
                    message: Should never log
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenReturn(List.of(Map.of("someKey", "someValue")));

        Automation automation = factory.createAutomation("yaml", yaml);

        EventContext context = EventContext.of(new TimeBasedEvent(LocalTime.of(14, 15)));
        engine.executeAutomation(automation, context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should never log"));
    }

    @Test
    void shouldNotTrigger_whenQueryReturnsEmptyResult() {
        var yaml = """
                alias: Empty result test
                triggers:
                  - trigger: onJdbcQuery
                    query: SELECT * FROM tasks
                    expression: "{{ result.size > 0 }}"
                actions:
                  - action: logger
                    message: Should not trigger
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenReturn(List.of());

        Automation automation = factory.createAutomation("yaml", yaml);

        EventContext context = EventContext.of(new TimeBasedEvent(LocalTime.of(15, 30)));
        engine.executeAutomation(automation, context);

        assertThat(automation.anyTriggerActivated(context)).isFalse();
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not trigger"));
    }

    @Test
    void shouldThrow_whenExpressionEvaluationFails() {
        var yaml = """
                alias: Invalid expression
                triggers:
                  - trigger: onJdbcQuery
                    query: SELECT COUNT(*) as count FROM tasks
                    expression: "{{ result[0].count > }}"
                actions:
                  - action: logger
                    message: Should not reach here
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenReturn(List.of(Map.of("count", 2)));

        Automation automation = factory.createAutomation("yaml", yaml);

        EventContext context = EventContext.of(new TimeBasedEvent(LocalTime.of(18, 0)));

        assertThatThrownBy(() -> engine.executeAutomation(automation, context))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing expression");
    }

    @Test
    void shouldThrow_whenJdbcQueryFails() {
        var yaml = """
                alias: JDBC failure
                triggers:
                  - trigger: onJdbcQuery
                    query: SELECT * FROM tasks
                    expression: "{{ result.size > 0 }}"
                actions:
                  - action: logger
                    message: Should not log
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenThrow(new RuntimeException("Database unreachable"));

        Automation automation = factory.createAutomation("yaml", yaml);

        EventContext context = EventContext.of(new TimeBasedEvent(LocalTime.of(19, 0)));

        assertThatThrownBy(() -> engine.executeAutomation(automation, context))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database unreachable");
    }
}
