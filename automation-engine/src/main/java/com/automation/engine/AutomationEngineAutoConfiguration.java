package com.automation.engine;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.events.publisher.IEventPublisher;
import com.automation.engine.factory.actions.ActionResolver;
import com.automation.engine.factory.actions.IActionSupplier;
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
    public ActionResolver actionResolver(IActionSupplier supplier, List<IActionInterceptor> actionInterceptors) {
        return new ActionResolver(supplier, actionInterceptors);
    }
}