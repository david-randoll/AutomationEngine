package com.automation.engine.config;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.publisher.IEventPublisher;
import com.automation.engine.creator.AutomationCreator;
import com.automation.engine.creator.AutomationProcessor;
import com.automation.engine.creator.actions.ActionBuilder;
import com.automation.engine.creator.conditions.ConditionBuilder;
import com.automation.engine.creator.parsers.AutomationParserRouter;
import com.automation.engine.creator.parsers.ManualAutomationBuilder;
import com.automation.engine.creator.triggers.TriggerBuilder;
import com.automation.engine.creator.variables.VariableBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfig {
    @Bean
    @ConditionalOnMissingBean
    public AutomationEngine automationEngine(IEventPublisher publisher) {
        return new AutomationEngine(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationProcessor automationProcessor(
            ActionBuilder actionBuilder, ConditionBuilder conditionBuilder,
            TriggerBuilder triggerBuilder, VariableBuilder variableBuilder
    ) {
        return new AutomationProcessor(actionBuilder, conditionBuilder, triggerBuilder, variableBuilder);
    }

    @Bean("manualAutomationParser")
    @ConditionalOnMissingBean
    public ManualAutomationBuilder manualAutomationBuilder(AutomationProcessor processor) {
        return new ManualAutomationBuilder(processor);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationCreator automationCreator(ManualAutomationBuilder builder, AutomationParserRouter router) {
        return new AutomationCreator(builder, router);
    }
}
