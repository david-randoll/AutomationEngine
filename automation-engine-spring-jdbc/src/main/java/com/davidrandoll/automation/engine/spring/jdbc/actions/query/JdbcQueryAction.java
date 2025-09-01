package com.davidrandoll.automation.engine.spring.jdbc.actions.query;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JdbcQueryAction extends PluggableAction<JdbcQueryActionContext> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void doExecute(EventContext ec, JdbcQueryActionContext ac) {
        if (ac.getQuery() == null || ac.getQuery().isBlank()) {
            log.warn("JdbcQueryAction skipped: query is null or blank");
            return;
        }
        if (ac.getVariable() == null || ac.getVariable().isBlank()) {
            throw new IllegalArgumentException("Variable name must be provided for storing query result");
        }

        try {
            Object result;

            if ("single".equalsIgnoreCase(ac.getMode())) {
                // Query for single object (Map<String, Object>)
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(ac.getQuery(), ac.getParams());
                result = rows.isEmpty() ? null : rows.getFirst();

            } else if ("list".equalsIgnoreCase(ac.getMode())) {
                // Query for list of rows
                result = jdbcTemplate.queryForList(ac.getQuery(), ac.getParams());
            } else {
                throw new IllegalArgumentException("Unknown mode: " + ac.getMode() + ". Supported modes: list, single");
            }

            // Store result in automation variable
            ec.addMetadata(ac.getVariable(), result);
            log.info("JdbcQueryAction executed and stored result in variable '{}'", ac.getVariable());

        } catch (Exception e) {
            throw new RuntimeException("Failed to execute JDBC query: " + ac.getQuery(), e);
        }
    }
}
