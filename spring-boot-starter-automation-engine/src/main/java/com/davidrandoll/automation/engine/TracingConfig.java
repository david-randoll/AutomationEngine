package com.davidrandoll.automation.engine;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionInterceptor;
import com.davidrandoll.automation.engine.tracing.TracingAppender;
import com.davidrandoll.automation.engine.tracing.interceptors.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Configuration for automation tracing interceptors.
 * <p>
 * Tracing interceptors are always available but require both:
 * 1. Global configuration: <code>automation.engine.tracing.enabled=true</code>
 * (read by TracingExecutionInterceptor)
 * 2. Per-automation flag: <code>tracingEnabled: true</code> in YAML/JSON
 * definition
 * </p>
 */
@Configuration
@ConditionalOnProperty(prefix = "automation-engine.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TracingConfig {

    @Order(-2)
    @Bean("tracingExecutionInterceptor")
    @ConditionalOnMissingBean(name = "tracingExecutionInterceptor", ignored = TracingExecutionInterceptor.class)
    public IAutomationExecutionInterceptor tracingExecutionInterceptor() {
        // Tracing is enabled by default when this bean is created
        // Individual automations control tracing via their options.tracing flag
        return new TracingExecutionInterceptor(true);
    }

    @Order(-2)
    @Bean("tracingVariableInterceptor")
    @ConditionalOnMissingBean(name = "tracingVariableInterceptor", ignored = TracingVariableInterceptor.class)
    public IVariableInterceptor tracingVariableInterceptor() {
        return new TracingVariableInterceptor();
    }

    @Order(-2)
    @Bean("tracingTriggerInterceptor")
    @ConditionalOnMissingBean(name = "tracingTriggerInterceptor", ignored = TracingTriggerInterceptor.class)
    public ITriggerInterceptor tracingTriggerInterceptor() {
        return new TracingTriggerInterceptor();
    }

    @Order(-2)
    @Bean("tracingConditionInterceptor")
    @ConditionalOnMissingBean(name = "tracingConditionInterceptor", ignored = TracingConditionInterceptor.class)
    public IConditionInterceptor tracingConditionInterceptor() {
        return new TracingConditionInterceptor();
    }

    @Order(-2)
    @Bean("tracingActionInterceptor")
    @ConditionalOnMissingBean(name = "tracingActionInterceptor", ignored = TracingActionInterceptor.class)
    public IActionInterceptor tracingActionInterceptor() {
        return new TracingActionInterceptor();
    }

    @Order(-2)
    @Bean("tracingResultInterceptor")
    @ConditionalOnMissingBean(name = "tracingResultInterceptor", ignored = TracingResultInterceptor.class)
    public IResultInterceptor tracingResultInterceptor(ObjectMapper objectMapper) {
        return new TracingResultInterceptor(objectMapper);
    }

    @Bean
    public TracingAppender tracingAppender() {
        TracingAppender appender = new TracingAppender();
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        appender.setContext(lc);
        appender.start();
        lc.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
        return appender;
    }
}
