package com.davidrandoll.automation.engine.jdbc.actions.execute;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class JdbcExecuteAction extends PluggableAction<JdbcExecuteActionContext> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void doExecute(EventContext ec, JdbcExecuteActionContext ac) {
        if (ac.getQuery() == null || ac.getQuery().isBlank()) {
            log.warn("JdbcExecuteAction skipped: query is null or blank");
            return;
        }

        try {
            int affected = jdbcTemplate.update(ac.getQuery(), ac.getParams());
            log.info("JdbcExecuteAction executed. Rows affected: {}", affected);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute JDBC query: " + ac.getQuery(), e);
        }
    }
}