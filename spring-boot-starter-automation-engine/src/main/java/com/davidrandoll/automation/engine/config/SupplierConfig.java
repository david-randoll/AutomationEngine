package com.davidrandoll.automation.engine.config;

import com.davidrandoll.automation.engine.creator.actions.IActionSupplier;
import com.davidrandoll.automation.engine.creator.conditions.IConditionSupplier;
import com.davidrandoll.automation.engine.creator.result.IResultSupplier;
import com.davidrandoll.automation.engine.creator.triggers.ITriggerSupplier;
import com.davidrandoll.automation.engine.creator.variables.IVariableSupplier;
import com.davidrandoll.automation.engine.suppliers.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupplierConfig {
    @Bean
    @ConditionalOnMissingBean(value = IActionSupplier.class, ignored = SpringActionSupplier.class)
    public IActionSupplier actionSupplier(ApplicationContext applicationContext) {
        return new SpringActionSupplier(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(value = IConditionSupplier.class, ignored = SpringConditionSupplier.class)
    public IConditionSupplier conditionSupplier(ApplicationContext applicationContext) {
        return new SpringConditionSupplier(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(value = IResultSupplier.class, ignored = SpringResultSupplier.class)
    public IResultSupplier resultSupplier(ApplicationContext applicationContext) {
        return new SpringResultSupplier(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(value = ITriggerSupplier.class, ignored = SpringTriggerSupplier.class)
    public ITriggerSupplier triggerSupplier(ApplicationContext applicationContext) {
        return new SpringTriggerSupplier(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(value = IVariableSupplier.class, ignored = SpringVariableSupplier.class)
    public IVariableSupplier variableSupplier(ApplicationContext applicationContext) {
        return new SpringVariableSupplier(applicationContext);
    }
}
