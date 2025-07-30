package com.davidrandoll.automation.engine.config;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.publisher.IEventPublisher;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
import com.davidrandoll.automation.engine.creator.AutomationProcessor;
import com.davidrandoll.automation.engine.creator.actions.ActionBuilder;
import com.davidrandoll.automation.engine.creator.conditions.ConditionBuilder;
import com.davidrandoll.automation.engine.creator.events.EventFactory;
import com.davidrandoll.automation.engine.creator.parsers.AutomationParserRouter;
import com.davidrandoll.automation.engine.creator.parsers.ManualAutomationBuilder;
import com.davidrandoll.automation.engine.creator.result.ResultBuilder;
import com.davidrandoll.automation.engine.creator.triggers.TriggerBuilder;
import com.davidrandoll.automation.engine.creator.variables.VariableBuilder;
import com.davidrandoll.automation.engine.orchestrator.AutomationOrchestrator;
import com.davidrandoll.automation.engine.orchestrator.IAEOrchestrator;
import com.davidrandoll.automation.engine.orchestrator.interceptors.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CoreConfig {

    @Bean
    @ConditionalOnMissingBean
    public IAutomationExecutionInterceptor loggingExecutionInterceptor() {
        return new LoggingExecutionInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public IAutomationHandleEventInterceptor loggingHandleEventInterceptor() {
        return new LoggingHandleEventInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public IAEOrchestrator automationOrchestrator(IEventPublisher publisher,
                                                  List<IAutomationExecutionInterceptor> executionInterceptors,
                                                  List<IAutomationHandleEventInterceptor> handleEventInterceptors) {
        var orchestrator = new AutomationOrchestrator(publisher);
        return new InterceptingAutomationOrchestrator(orchestrator, executionInterceptors, handleEventInterceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationEngine automationEngine(IAEOrchestrator orchestrator, AutomationFactory creator, EventFactory eventFactory) {
        return new AutomationEngine(orchestrator, creator, eventFactory);
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
    public AutomationFactory automationCreator(ManualAutomationBuilder builder, AutomationParserRouter router) {
        return new AutomationFactory(builder, router);
    }
}
