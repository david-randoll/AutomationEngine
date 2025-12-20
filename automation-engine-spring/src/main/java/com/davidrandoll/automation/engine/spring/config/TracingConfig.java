package com.davidrandoll.automation.engine.spring.config;

import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionInterceptor;
import com.davidrandoll.automation.engine.spring.tracing.TracingConfigurationProperties;
import com.davidrandoll.automation.engine.spring.tracing.interceptors.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for automation execution tracing.
 * Registers all tracing interceptors when tracing is enabled.
 */
@Configuration
@EnableConfigurationProperties(TracingConfigurationProperties.class)
public class TracingConfig {

    /**
     * Top-level automation execution tracing interceptor.
     * Coordinates all component-level traces and generates trace IDs.
     */
    @Bean
    @ConditionalOnProperty(name = "automation-engine.tracing.enabled", havingValue = "true")
    public IAutomationExecutionInterceptor automationTracingInterceptor(TracingConfigurationProperties config) {
        return new AutomationTracingInterceptor(config);
    }

    /**
     * Variable resolution tracing interceptor.
     */
    @Bean
    @ConditionalOnProperty(name = "automation-engine.tracing.enabled", havingValue = "true")
    public IVariableInterceptor variableTracingInterceptor() {
        return new VariableTracingInterceptor();
    }

    /**
     * Trigger evaluation tracing interceptor.
     */
    @Bean
    @ConditionalOnProperty(name = "automation-engine.tracing.enabled", havingValue = "true")
    public ITriggerInterceptor triggerTracingInterceptor() {
        return new TriggerTracingInterceptor();
    }

    /**
     * Condition evaluation tracing interceptor.
     */
    @Bean
    @ConditionalOnProperty(name = "automation-engine.tracing.enabled", havingValue = "true")
    public IConditionInterceptor conditionTracingInterceptor() {
        return new ConditionTracingInterceptor();
    }

    /**
     * Action execution tracing interceptor.
     */
    @Bean
    @ConditionalOnProperty(name = "automation-engine.tracing.enabled", havingValue = "true")
    public IActionInterceptor actionTracingInterceptor() {
        return new ActionTracingInterceptor();
    }

    /**
     * Result computation tracing interceptor.
     */
    @Bean
    @ConditionalOnProperty(name = "automation-engine.tracing.enabled", havingValue = "true")
    public IResultInterceptor resultTracingInterceptor() {
        return new ResultTracingInterceptor();
    }
}