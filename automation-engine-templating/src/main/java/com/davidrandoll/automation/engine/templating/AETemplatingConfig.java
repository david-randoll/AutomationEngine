package com.davidrandoll.automation.engine.templating;

import com.davidrandoll.automation.engine.templating.interceptors.*;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pebbletemplates.pebble.PebbleEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class AETemplatingConfig {
    @Bean(name = "actionTemplatingInterceptor")
    @Order(-1)
    @ConditionalOnMissingBean(name = "actionTemplatingInterceptor", ignored = ActionTemplatingInterceptor.class)
    public ActionTemplatingInterceptor actionTemplatingInterceptor(JsonNodeVariableProcessor processor, ObjectMapper objectMapper) {
        return new ActionTemplatingInterceptor(processor, objectMapper);
    }

    @Bean(name = "conditionTemplatingInterceptor")
    @Order(-1)
    @ConditionalOnMissingBean(name = "conditionTemplatingInterceptor", ignored = ConditionTemplatingInterceptor.class)
    public ConditionTemplatingInterceptor conditionTemplatingInterceptor(JsonNodeVariableProcessor processor, ObjectMapper objectMapper) {
        return new ConditionTemplatingInterceptor(processor, objectMapper);
    }

    @Bean(name = "resultTemplatingInterceptor")
    @Order(-1)
    @ConditionalOnMissingBean(name = "resultTemplatingInterceptor", ignored = ResultTemplatingInterceptor.class)
    public ResultTemplatingInterceptor resultTemplatingInterceptor(JsonNodeVariableProcessor processor, ObjectMapper objectMapper) {
        return new ResultTemplatingInterceptor(processor, objectMapper);
    }

    @Bean(name = "triggerTemplatingInterceptor")
    @Order(-1)
    @ConditionalOnMissingBean(name = "triggerTemplatingInterceptor", ignored = TriggerTemplatingInterceptor.class)
    public TriggerTemplatingInterceptor triggerTemplatingInterceptor(JsonNodeVariableProcessor processor, ObjectMapper objectMapper) {
        return new TriggerTemplatingInterceptor(processor, objectMapper);
    }

    @Bean(name = "variableTemplatingInterceptor")
    @Order(-1)
    @ConditionalOnMissingBean(name = "variableTemplatingInterceptor", ignored = VariableTemplatingInterceptor.class)
    public VariableTemplatingInterceptor variableTemplatingInterceptor(JsonNodeVariableProcessor processor, ObjectMapper objectMapper) {
        return new VariableTemplatingInterceptor(processor, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonNodeVariableProcessor jsonNodeVariableProcessor(TemplateProcessor templateProcessor, ObjectMapper mapper) {
        return new JsonNodeVariableProcessor(templateProcessor, mapper);
    }

    @Bean(name = "templateProcessor")
    @ConditionalOnMissingBean(name = "templateProcessor", ignored = TemplateProcessor.class)
    public TemplateProcessor templateProcessor(PebbleEngine pebbleEngine) {
        return new TemplateProcessor(pebbleEngine);
    }
}
