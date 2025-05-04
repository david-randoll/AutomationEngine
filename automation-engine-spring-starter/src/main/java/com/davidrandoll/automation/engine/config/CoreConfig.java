package com.davidrandoll.automation.engine.config;

import com.davidrandoll.automation.engine.core.AutomationEngine;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.events.publisher.IEventPublisher;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.davidrandoll.automation.engine.creator.AutomationCreator;
import com.davidrandoll.automation.engine.creator.AutomationProcessor;
import com.davidrandoll.automation.engine.creator.actions.ActionBuilder;
import com.davidrandoll.automation.engine.creator.actions.IActionSupplier;
import com.davidrandoll.automation.engine.creator.conditions.ConditionBuilder;
import com.davidrandoll.automation.engine.creator.conditions.IConditionSupplier;
import com.davidrandoll.automation.engine.creator.parsers.IAutomationFormatParser;
import com.davidrandoll.automation.engine.creator.parsers.ManualAutomationBuilder;
import com.davidrandoll.automation.engine.creator.parsers.json.IJsonConverter;
import com.davidrandoll.automation.engine.creator.parsers.json.JsonAutomationParser;
import com.davidrandoll.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.davidrandoll.automation.engine.creator.parsers.yaml.YamlAutomationParser;
import com.davidrandoll.automation.engine.creator.triggers.ITriggerSupplier;
import com.davidrandoll.automation.engine.creator.triggers.TriggerBuilder;
import com.davidrandoll.automation.engine.creator.variables.IVariableSupplier;
import com.davidrandoll.automation.engine.creator.variables.VariableBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class CoreConfig {
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

    @Bean("manualAutomationParser")
    @ConditionalOnMissingBean
    public ManualAutomationBuilder manualAutomationBuilder(AutomationProcessor processor) {
        return new ManualAutomationBuilder(processor);
    }

    @Bean("jsonAutomationParser")
    @ConditionalOnMissingBean
    public JsonAutomationParser jsonAutomationParser(ManualAutomationBuilder builder, IJsonConverter converter) {
        return new JsonAutomationParser(builder, converter);
    }

    @Bean("yamlAutomationParser")
    @ConditionalOnMissingBean
    public YamlAutomationParser yamlAutomationParser(ManualAutomationBuilder builder, IYamlConverter converter) {
        return new YamlAutomationParser(builder, converter);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationCreator automationCreator(ManualAutomationBuilder builder, Map<String, IAutomationFormatParser<?>> formatParsers) {
        return new AutomationCreator(builder, formatParsers);
    }
}
