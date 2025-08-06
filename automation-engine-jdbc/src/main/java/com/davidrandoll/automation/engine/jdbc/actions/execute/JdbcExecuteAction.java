package com.davidrandoll.automation.engine.jdbc.actions.execute;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("jdbcExecuteAction")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "jdbcExecuteAction", ignored = JdbcExecuteAction.class)
@ConditionalOnClass(NamedParameterJdbcTemplate.class)
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