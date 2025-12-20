package com.davidrandoll.automation.engine.spring.config;

import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.core.tracing.interceptors.*;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionInterceptor;
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
@ConditionalOnProperty(prefix = "automation-engine.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TracingConfig {

    @Bean("tracingExecutionInterceptor")
    @ConditionalOnMissingBean(name = "tracingExecutionInterceptor", ignored = TracingExecutionInterceptor.class)
    public IAutomationExecutionInterceptor tracingExecutionInterceptor() {
        // Tracing is enabled by default when this bean is created
        // Individual automations control tracing via their options.tracing flag
        return new TracingExecutionInterceptor(true);
    }

    @Bean("tracingVariableInterceptor")
    @ConditionalOnMissingBean(name = "tracingVariableInterceptor", ignored = TracingVariableInterceptor.class)
    public IVariableInterceptor tracingVariableInterceptor() {
        return new TracingVariableInterceptor();
    }

    @Bean("tracingTriggerInterceptor")
    @ConditionalOnMissingBean(name = "tracingTriggerInterceptor", ignored = TracingTriggerInterceptor.class)
    public ITriggerInterceptor tracingTriggerInterceptor() {
        return new TracingTriggerInterceptor();
    }

    @Bean("tracingConditionInterceptor")
    @ConditionalOnMissingBean(name = "tracingConditionInterceptor", ignored = TracingConditionInterceptor.class)
    public IConditionInterceptor tracingConditionInterceptor() {
        return new TracingConditionInterceptor();
    }

    @Bean("tracingActionInterceptor")
    @ConditionalOnMissingBean(name = "tracingActionInterceptor", ignored = TracingActionInterceptor.class)
    public IActionInterceptor tracingActionInterceptor() {
        return new TracingActionInterceptor();
    }

    @Bean("tracingResultInterceptor")
    @ConditionalOnMissingBean(name = "tracingResultInterceptor", ignored = TracingResultInterceptor.class)
    public IResultInterceptor tracingResultInterceptor() {
        return new TracingResultInterceptor();
    }
}
