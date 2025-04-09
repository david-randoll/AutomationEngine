package com.automation.engine;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.automation.engine.core.events.publisher.IEventPublisher;
import com.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.automation.engine.factory.AutomationProcessor;
import com.automation.engine.factory.actions.ActionBuilder;
import com.automation.engine.factory.actions.IActionSupplier;
import com.automation.engine.factory.conditions.ConditionBuilder;
import com.automation.engine.factory.conditions.IConditionSupplier;
import com.automation.engine.factory.triggers.ITriggerSupplier;
import com.automation.engine.factory.triggers.TriggerBuilder;
import com.automation.engine.factory.variables.IVariableSupplier;
import com.automation.engine.factory.variables.VariableBuilder;
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
    public ActionBuilder actionBuilder(IActionSupplier supplier, List<IActionInterceptor> interceptors) {
        return new ActionBuilder(supplier, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConditionBuilder conditionBuilder(IConditionSupplier supplier, List<IConditionInterceptor> interceptors) {
        return new ConditionBuilder(supplier, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public TriggerBuilder triggerBuilder(ITriggerSupplier supplier, List<ITriggerInterceptor> interceptors) {
        return new TriggerBuilder(supplier, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public VariableBuilder variableBuilder(IVariableSupplier supplier, List<IVariableInterceptor> interceptors) {
        return new VariableBuilder(supplier, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationProcessor automationProcessor(
            ActionBuilder actionBuilder,
            ConditionBuilder conditionBuilder,
            TriggerBuilder triggerBuilder,
            VariableBuilder variableBuilder
    ) {
        return new AutomationProcessor(actionBuilder, conditionBuilder, triggerBuilder, variableBuilder);
    }
}