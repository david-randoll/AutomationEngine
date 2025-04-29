package com.automation.engine;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.automation.engine.core.events.publisher.IEventPublisher;
import com.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.automation.engine.creator.AutomationCreator;
import com.automation.engine.creator.AutomationProcessor;
import com.automation.engine.creator.actions.ActionBuilder;
import com.automation.engine.creator.actions.IActionSupplier;
import com.automation.engine.creator.conditions.ConditionBuilder;
import com.automation.engine.creator.conditions.IConditionSupplier;
import com.automation.engine.creator.parsers.IAutomationFormatParser;
import com.automation.engine.creator.parsers.ManualAutomationBuilder;
import com.automation.engine.creator.parsers.json.IJsonConverter;
import com.automation.engine.creator.parsers.json.JsonAutomationParser;
import com.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.automation.engine.creator.parsers.yaml.YamlAutomationParser;
import com.automation.engine.creator.triggers.ITriggerSupplier;
import com.automation.engine.creator.triggers.TriggerBuilder;
import com.automation.engine.creator.variables.IVariableSupplier;
import com.automation.engine.creator.variables.VariableBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Map;

@AutoConfiguration
@ComponentScan
@ConfigurationPropertiesScan
@EnableScheduling
public class AutomationEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AEConfigProvider automationEngineConfigProvider() {
        return new AEConfigProvider();
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