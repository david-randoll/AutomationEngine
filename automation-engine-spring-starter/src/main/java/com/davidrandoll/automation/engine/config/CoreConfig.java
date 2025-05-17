package com.davidrandoll.automation.engine.config;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.AutomationHandler;
import com.davidrandoll.automation.engine.core.events.publisher.IEventPublisher;
import com.davidrandoll.automation.engine.creator.AutomationCreator;
import com.davidrandoll.automation.engine.creator.AutomationProcessor;
import com.davidrandoll.automation.engine.creator.actions.ActionBuilder;
import com.davidrandoll.automation.engine.creator.conditions.ConditionBuilder;
import com.davidrandoll.automation.engine.creator.events.EventFactory;
import com.davidrandoll.automation.engine.creator.parsers.AutomationParserRouter;
import com.davidrandoll.automation.engine.creator.parsers.ManualAutomationBuilder;
import com.davidrandoll.automation.engine.creator.result.ResultBuilder;
import com.davidrandoll.automation.engine.creator.triggers.TriggerBuilder;
import com.davidrandoll.automation.engine.creator.variables.VariableBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfig {
    @Bean
    @ConditionalOnMissingBean
    public AutomationHandler automationHandler(IEventPublisher publisher) {
        return new AutomationHandler(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationEngine automationEngine(AutomationHandler handler, AutomationCreator creator, EventFactory eventFactory) {
        return new AutomationEngine(handler, creator, eventFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationProcessor automationProcessor(
            ActionBuilder actionBuilder, ConditionBuilder conditionBuilder,
            TriggerBuilder triggerBuilder, VariableBuilder variableBuilder,
            ResultBuilder resultBuilder
    ) {
        return new AutomationProcessor(actionBuilder, conditionBuilder, triggerBuilder, variableBuilder, resultBuilder);
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
