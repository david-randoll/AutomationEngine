package com.davidrandoll.automation.engine.jdbc.conditions.on_query;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.jdbc.TestConfig;
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
class OnJdbcQueryConditionTest extends AutomationEngineTest {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetMocks() {
        reset(jdbcTemplate);
    }

    @Test
    void shouldExecuteAction_whenJdbcQueryConditionEvaluatesTrue() {
        var yaml = """
                alias: Action when tasks pending
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onJdbcQuery
                    query: "SELECT COUNT(*) as count FROM tasks WHERE completed = false"
                    expression: "{{ result[0].count > 0 }}"
                actions:
                  - action: logger
                    message: Tasks pending
                """;

        when(jdbcTemplate.queryForList(
                eq("SELECT COUNT(*) as count FROM tasks WHERE completed = false"),
                any(Map.class)
        )).thenReturn(List.of(Map.of("count", 3)));

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(11, 30)));
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue();
        assertThat(logAppender.getLoggedMessages()).anyMatch(msg -> msg.contains("Tasks pending"));
    }

    @Test
    void shouldSkipAction_whenJdbcQueryConditionIsFalse() {
        var yaml = """
                alias: No action if no tasks
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onJdbcQuery
                    query: "SELECT COUNT(*) as count FROM tasks WHERE completed = false"
                    expression: "{{ result[0].count > 0 }}"
                actions:
                  - action: logger
                    message: Should not log this
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenReturn(List.of(Map.of("count", 0)));

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(11, 30)));
        engine.publishEvent(context);

        assertThat(automation.anyTriggerActivated(context)).isTrue(); // trigger still fires
        assertThat(logAppender.getLoggedMessages()).noneMatch(msg -> msg.contains("Should not log this"));
    }

    @Test
    void shouldNotExecuteAction_whenQueryIsMissing() {
        var yaml = """
                alias: Missing query
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onJdbcQuery
                    expression: "{{ result[0].count > 0 }}"
                actions:
                  - action: logger
                    message: Should never log
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(11, 30)));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should never log"));
    }

    @Test
    void shouldNotExecuteAction_whenExpressionIsMissing() {
        var yaml = """
                alias: Missing expression
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onJdbcQuery
                    query: "SELECT * FROM tasks"
                actions:
                  - action: logger
                    message: Should never log
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenReturn(List.of(Map.of("id", 123)));

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(11, 30)));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should never log"));
    }

    @Test
    void shouldThrow_whenExpressionIsInvalid() {
        var yaml = """
                alias: Invalid expression test
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onJdbcQuery
                    query: "SELECT COUNT(*) as count FROM tasks"
                    expression: "{{ result[0].count > }}"
                actions:
                  - action: logger
                    message: Should not log
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenReturn(List.of(Map.of("count", 1)));

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(11, 30)));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing expression");

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not log"));
    }

    @Test
    void shouldThrow_whenJdbcQueryFails() {
        var yaml = """
                alias: Query failure
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onJdbcQuery
                    query: "SELECT * FROM non_existing_table"
                    expression: "{{ result.size > 0 }}"
                actions:
                  - action: logger
                    message: Should not run
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenThrow(new RuntimeException("SQL error"));

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(11, 30)));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SQL error");
    }

    @Test
    void shouldNotExecuteAction_whenQueryReturnsEmptyList() {
        var yaml = """
                alias: Empty result
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: onJdbcQuery
                    query: SELECT * FROM tasks
                    expression: "{{ result.size > 0 }}"
                actions:
                  - action: logger
                    message: Should not log
                """;

        when(jdbcTemplate.queryForList(any(String.class), any(Map.class)))
                .thenReturn(List.of());

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(11, 30)));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Should not log"));
    }
}