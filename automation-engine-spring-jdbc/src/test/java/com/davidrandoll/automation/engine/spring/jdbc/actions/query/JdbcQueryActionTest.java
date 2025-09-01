package com.davidrandoll.automation.engine.spring.jdbc.actions.query;

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
import static org.mockito.Mockito.*;

@Import(TestConfig.class)
class JdbcQueryActionTest extends AutomationEngineTest {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetMocks() {
        reset(jdbcTemplate);
    }

    @Test
    void testListModeStoresListInVariable() {
        var yaml = """
                alias: Query multiple rows
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcQuery
                    query: "SELECT id, name FROM users WHERE active = true"
                    mode: list
                    variable: usersList
                """;

        List<Map<String, Object>> mockResult = List.of(
                Map.of("id", 1, "name", "Alice"),
                Map.of("id", 2, "name", "Bob")
        );

        when(jdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(mockResult);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        EventContext ctx = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(ctx);

        // Assert variable set correctly
        assertThat(ctx.getMetadata()).containsKey("usersList");
        assertThat(ctx.getMetadata("usersList")).isEqualTo(mockResult);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("JdbcQueryAction executed and stored result in variable 'usersList'"));
    }

    @Test
    void testSingleModeStoresFirstRowInVariable() {
        var yaml = """
                alias: Query single row
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcQuery
                    query: "SELECT count(*) AS total FROM tasks WHERE completed = false"
                    mode: single
                    variable: pendingTasksCount
                """;

        List<Map<String, Object>> mockResult = List.of(
                Map.of("total", 5)
        );

        when(jdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(mockResult);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        EventContext ctx = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 5)));
        engine.publishEvent(ctx);

        assertThat(ctx.getMetadata()).containsKey("pendingTasksCount");
        assertThat(ctx.getMetadata("pendingTasksCount")).isEqualTo(mockResult.get(0));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("JdbcQueryAction executed and stored result in variable 'pendingTasksCount'"));
    }

    @Test
    void testSingleModeStoresNullIfNoRows() {
        var yaml = """
                alias: Query single row empty result
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcQuery
                    query: "SELECT * FROM tasks WHERE id = 99999"
                    mode: single
                    variable: task
                """;

        when(jdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of());

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        EventContext ctx = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 10)));
        engine.publishEvent(ctx);

        assertThat(ctx.getMetadata()).containsKey("task");
        assertThat(ctx.getMetadata("task")).isNull();
    }

    @Test
    void testDefaultVariableNameUsedIfMissing() {
        var yaml = """
                alias: Default variable name test
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcQuery
                    query: "SELECT id FROM users"
                """;

        List<Map<String, Object>> mockResult = List.of(
                Map.of("id", 42)
        );

        when(jdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(mockResult);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        EventContext ctx = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 15)));
        engine.publishEvent(ctx);

        assertThat(ctx.getMetadata()).containsKey("queryResult");
        assertThat(ctx.getMetadata("queryResult")).isEqualTo(mockResult);
    }

    @Test
    void testMissingQuerySkipsExecution() {
        var yaml = """
                alias: Missing query
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcQuery
                    variable: myVar
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        EventContext ctx = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 20)));
        engine.publishEvent(ctx);

        verify(jdbcTemplate, never()).queryForList(anyString(), anyMap());

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("JdbcQueryAction skipped: query is null or blank"));
    }

    @Test
    void testMissingVariableNameThrows() {
        var yaml = """
                alias: Missing variable name
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcQuery
                    query: "SELECT 1"
                    variable: ""
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        EventContext ctx = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 25)));

        assertThatThrownBy(() -> engine.publishEvent(ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Variable name must be provided");

        verify(jdbcTemplate, never()).queryForList(anyString(), anyMap());
    }

    @Test
    void testQueryResultUsedInPebbleExpression() {
        var yaml = """
                alias: Use query result in Pebble expression
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcQuery
                    query: "SELECT id, name FROM users"
                    mode: list
                    variable: usersList
                  - action: logger
                    message: "First user is {{ usersList[0].name }}"
                """;

        List<Map<String, Object>> mockResult = List.of(
                Map.of("id", 1, "name", "Alice"),
                Map.of("id", 2, "name", "Bob")
        );

        when(jdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(mockResult);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        EventContext ctx = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 35)));
        engine.publishEvent(ctx);

        assertThat(ctx.getMetadata()).containsKey("usersList");
        assertThat(ctx.getMetadata("usersList")).isEqualTo(mockResult);
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("First user is Alice"));
    }

    @Test
    void testSingleQueryResultUsedInPebbleExpression() {
        var yaml = """
                alias: Use single query result in Pebble expression
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcQuery
                    query: "SELECT id, name FROM users WHERE id = 1"
                    mode: single
                    variable: user
                  - action: logger
                    message: "Queried user is {{ user.name }}"
                """;

        Map<String, Object> mockRow = Map.of("id", 1, "name", "Alice");

        // Return a single row in the list
        when(jdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of(mockRow));

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        EventContext ctx = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 40)));
        engine.publishEvent(ctx);

        assertThat(ctx.getMetadata()).containsKey("user");
        assertThat(ctx.getMetadata("user")).isEqualTo(mockRow);
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Queried user is Alice"));
    }


}