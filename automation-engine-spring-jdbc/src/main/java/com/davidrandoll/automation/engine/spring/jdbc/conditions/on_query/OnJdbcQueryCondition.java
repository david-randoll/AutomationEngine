package com.davidrandoll.automation.engine.spring.jdbc.conditions.on_query;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class OnJdbcQueryCondition extends PluggableCondition<OnJdbcQueryConditionContext> {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TemplateProcessor templateProcessor;

    @Override
    public boolean autoEvaluateExpression() {
        return false;
    }

    @Override
    public boolean isSatisfied(EventContext ec, OnJdbcQueryConditionContext cc) {
        if (cc.getQuery() == null || cc.getExpression() == null) {
            return false;
        }

        List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(cc.getQuery(), cc.getParams());
        Map<String, Object> context = new HashMap<>();
        context.put("result", queryResult);
        context.put("event", ec);

        try {
            String result = (String) templateProcessor.process(cc.getExpression(), context);
            return Boolean.TRUE.toString().equalsIgnoreCase(result);
        } catch (Exception e) {
            throw new RuntimeException("Error processing expression: " + cc.getExpression(), e);
        }
    }
}