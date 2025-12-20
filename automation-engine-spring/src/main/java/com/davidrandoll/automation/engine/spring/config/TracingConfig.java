package com.davidrandoll.automation.engine.spring.config;

import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.core.tracing.interceptors.*;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.davidrandoll.automation.engine.orchestrator.interceptors.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for automation tracing interceptors.
 * <p>
 * Tracing interceptors are always available but require both:
 * 1. Global configuration: <code>automation.engine.tracing.enabled=true</code> (read by TracingExecutionInterceptor)
 * 2. Per-automation flag: <code>tracingEnabled: true</code> in YAML/JSON definition
 * </p>
 */
@Configuration
public class TracingConfig {

    @Bean
    @ConditionalOnMissingBean
    public IAutomationExecutionInterceptor tracingExecutionInterceptor() {
        // Tracing is disabled by default when this bean is created
        // Individual automations control tracing via their options.tracing flag
        return new TracingExecutionInterceptor(false);
    }

    @Bean
    @ConditionalOnMissingBean
    public IVariableInterceptor tracingVariableInterceptor() {
        return new TracingVariableInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ITriggerInterceptor tracingTriggerInterceptor() {
        return new TracingTriggerInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public IConditionInterceptor tracingConditionInterceptor() {
        return new TracingConditionInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public IActionInterceptor tracingActionInterceptor() {
        return new TracingActionInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public IResultInterceptor tracingResultInterceptor() {
        return new TracingResultInterceptor();
    }
}
