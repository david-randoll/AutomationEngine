package com.davidrandoll.automation.engine.jdbc.triggers.on_jdbc_query;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("onJdbcQueryTrigger")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "onJdbcQueryTrigger", ignored = OnJdbcQueryTrigger.class)
@ConditionalOnClass(NamedParameterJdbcTemplate.class)
public class OnJdbcQueryTrigger extends PluggableTrigger<OnJdbcQueryTriggerContext> {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TemplateProcessor pebble;

    @Override
    public boolean isTriggered(EventContext ec, OnJdbcQueryTriggerContext tc) {
        if (tc.getQuery() == null || tc.getExpression() == null) {
            return false;
        }

        List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(tc.getQuery(), tc.getParams());
        Map<String, Object> context = new HashMap<>();
        context.put("result", queryResult);
        context.put("event", ec);

        try {
            String result = pebble.process(tc.getExpression(), context);
            return Boolean.TRUE.toString().equalsIgnoreCase(result);
        } catch (IOException e) {
            throw new RuntimeException("Error processing expression: " + tc.getExpression(), e);
        }
    }
}