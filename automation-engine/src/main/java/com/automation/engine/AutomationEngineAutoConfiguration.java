package com.automation.engine;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.automation.engine.core.events.publisher.IEventPublisher;
import com.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.automation.engine.factory.AutomationResolver;
import com.automation.engine.factory.actions.ActionResolver;
import com.automation.engine.factory.actions.IActionSupplier;
import com.automation.engine.factory.conditions.ConditionResolver;
import com.automation.engine.factory.conditions.IConditionSupplier;
import com.automation.engine.factory.triggers.ITriggerSupplier;
import com.automation.engine.factory.triggers.TriggerResolver;
import com.automation.engine.factory.variables.IVariableSupplier;
import com.automation.engine.factory.variables.VariableResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@AutoConfiguration
@ComponentScan
@ConfigurationPropertiesScan
@EnableScheduling
public class AutomationEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AutomationEngineConfigProvider automationEngineConfigProvider() {
        return new AutomationEngineConfigProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationEngine automationEngine(IEventPublisher publisher) {
        return new AutomationEngine(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public ActionResolver actionResolver(IActionSupplier supplier, List<IActionInterceptor> interceptors) {
        return new ActionResolver(supplier, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConditionResolver conditionResolver(IConditionSupplier supplier, List<IConditionInterceptor> interceptors) {
        return new ConditionResolver(supplier, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public TriggerResolver triggerResolver(ITriggerSupplier supplier, List<ITriggerInterceptor> interceptors) {
        return new TriggerResolver(supplier, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public VariableResolver variableResolver(IVariableSupplier supplier, List<IVariableInterceptor> interceptors) {
        return new VariableResolver(supplier, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationResolver automationResolver(
            ActionResolver actionResolver,
            ConditionResolver conditionResolver,
            TriggerResolver triggerResolver,
            VariableResolver variableResolver
    ) {
        return new AutomationResolver(actionResolver, conditionResolver, triggerResolver, variableResolver);
    }
}