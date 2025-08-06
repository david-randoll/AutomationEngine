package com.davidrandoll.automation.engine.jdbc.actions.execute;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.jdbc.TestConfig;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(TestConfig.class)
class JdbcExecuteActionTest extends AutomationEngineTest {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetMocks() {
        reset(jdbcTemplate);
    }


    @Test
    void testSuccessfulExecution() {
        var yaml = """
                alias: Update task as complete
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcExecute
                    query: "UPDATE tasks SET completed = true WHERE id = :id"
                    params:
                      id: 5
                """;

        when(jdbcTemplate.update(any(String.class), any(Map.class))).thenReturn(1);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        verify(jdbcTemplate).update(eq("UPDATE tasks SET completed = true WHERE id = :id"), eq(Map.of("id", 5)));

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Rows affected: 1"));
    }

    @Test
    void testMissingQuerySkipsExecution() {
        var yaml = """
                alias: Missing query
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcExecute
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 30)));
        engine.publishEvent(context);

        verify(jdbcTemplate, never()).update(any(), anyMap());

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("JdbcExecuteAction skipped: query is null or blank"));
    }

    @Test
    void testQueryThrowsException() {
        var yaml = """
                alias: Error during SQL
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: jdbcExecute
                    query: "UPDATE tasks SET completed = true WHERE id = :id"
                    params:
                      id: 99
                """;

        when(jdbcTemplate.update(any(), anyMap())).thenThrow(new RuntimeException("SQL error"));

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(11, 0)));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to execute JDBC query");

        verify(jdbcTemplate).update(any(), anyMap());
    }
}