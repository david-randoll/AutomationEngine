package com.davidrandoll.automation.engine.jdbc;

import com.davidrandoll.automation.engine.jdbc.actions.execute.JdbcExecuteAction;
import com.davidrandoll.automation.engine.jdbc.actions.query.JdbcQueryAction;
import com.davidrandoll.automation.engine.jdbc.conditions.on_query.OnJdbcQueryCondition;
import com.davidrandoll.automation.engine.jdbc.triggers.on_query.OnJdbcQueryTrigger;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
@ConditionalOnClass(NamedParameterJdbcTemplate.class)
public class AEJdbcConfig {
    @Bean("jdbcExecuteAction")
    @ConditionalOnMissingBean(name = "jdbcExecuteAction", ignored = JdbcExecuteAction.class)
    public JdbcExecuteAction jdbcExecuteAction(NamedParameterJdbcTemplate jdbcTemplate) {
        return new JdbcExecuteAction(jdbcTemplate);
    }

    @Bean("jdbcQueryAction")
    @ConditionalOnMissingBean(name = "jdbcQueryAction", ignored = JdbcQueryAction.class)
    public JdbcQueryAction jdbcQueryAction(NamedParameterJdbcTemplate jdbcTemplate) {
        return new JdbcQueryAction(jdbcTemplate);
    }

    @Bean("onJdbcQueryCondition")
    @ConditionalOnMissingBean(name = "onJdbcQueryCondition", ignored = OnJdbcQueryCondition.class)
    public OnJdbcQueryCondition onJdbcQueryCondition(NamedParameterJdbcTemplate jdbcTemplate, TemplateProcessor pebble) {
        return new OnJdbcQueryCondition(jdbcTemplate, pebble);
    }

    @Bean("onJdbcQueryTrigger")
    @ConditionalOnMissingBean(name = "onJdbcQueryTrigger", ignored = OnJdbcQueryTrigger.class)
    public OnJdbcQueryTrigger onJdbcQueryTrigger(NamedParameterJdbcTemplate jdbcTemplate, TemplateProcessor pebble) {
        return new OnJdbcQueryTrigger(jdbcTemplate, pebble);
    }
}