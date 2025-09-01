package com.davidrandoll.automation.engine.templating;

import com.davidrandoll.automation.engine.templating.extensions.CustomExtension;
import com.davidrandoll.automation.engine.templating.extensions.filters.IntegerFilter;
import com.davidrandoll.automation.engine.templating.extensions.filters.NumberFormatFilter;
import com.davidrandoll.automation.engine.templating.extensions.filters.TimeFormatFilter;
import com.davidrandoll.automation.engine.templating.interceptors.*;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pebbletemplates.boot.autoconfigure.PebbleProperties;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.attributes.methodaccess.MethodAccessValidator;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Extension;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.spring.extension.SpringExtension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

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

    /*
     * Filters
     */
    @Bean("int")
    @ConditionalOnMissingBean(name = "int", ignored = IntegerFilter.class)
    public Filter integerFilter() {
        return new IntegerFilter();
    }

    @Bean("number_format")
    @ConditionalOnMissingBean(name = "number_format", ignored = NumberFormatFilter.class)
    public Filter numberFormatFilter() {
        return new NumberFormatFilter();
    }

    @Bean("time_format")
    @ConditionalOnMissingBean(name = "time_format", ignored = TimeFormatFilter.class)
    public Filter timeFormatFilter() {
        return new TimeFormatFilter();
    }

    @Bean("customExtension")
    @ConditionalOnMissingBean(name = "customExtension", ignored = CustomExtension.class)
    public AbstractExtension customExtension(Map<String, Filter> filters) {
        return new CustomExtension(filters);
    }

    @Bean
    @ConditionalOnMissingBean
    public PebbleEngine pebbleEngine(PebbleProperties properties,
                                     Loader<?> pebbleLoader,
                                     SpringExtension springExtension,
                                     @Nullable List<Extension> extensions,
                                     @Nullable MethodAccessValidator methodAccessValidator) {
        PebbleEngine.Builder builder = new PebbleEngine.Builder();
        builder.loader(pebbleLoader);
        builder.extension(springExtension);
        if (extensions != null && !extensions.isEmpty()) {
            builder.extension(extensions.toArray(new Extension[0]));
        }
        if (!properties.isCache()) {
            builder.cacheActive(false);
        }
        if (properties.getDefaultLocale() != null) {
            builder.defaultLocale(properties.getDefaultLocale());
        }
        builder.strictVariables(properties.isStrictVariables());
        builder.greedyMatchMethod(properties.isGreedyMatchMethod());
        if (methodAccessValidator != null) {
            builder.methodAccessValidator(methodAccessValidator);
        }
        builder.autoEscaping(false);
        return builder.build();
    }
}
