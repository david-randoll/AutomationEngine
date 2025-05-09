package com.automation.engine.config;

import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.automation.engine.creator.actions.ActionBuilder;
import com.automation.engine.creator.actions.IActionSupplier;
import com.automation.engine.creator.conditions.ConditionBuilder;
import com.automation.engine.creator.conditions.IConditionSupplier;
import com.automation.engine.creator.triggers.ITriggerSupplier;
import com.automation.engine.creator.triggers.TriggerBuilder;
import com.automation.engine.creator.variables.IVariableSupplier;
import com.automation.engine.creator.variables.VariableBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BuilderConfig {
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
}
