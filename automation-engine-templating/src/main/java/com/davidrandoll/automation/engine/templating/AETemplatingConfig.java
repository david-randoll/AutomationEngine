package com.davidrandoll.automation.engine.templating;

import com.davidrandoll.automation.engine.templating.interceptors.*;
import com.davidrandoll.automation.engine.templating.pebbles.PebbleTemplateEngine;
import com.davidrandoll.automation.engine.templating.pebbles.extensions.AEPebbleExtension;
import com.davidrandoll.automation.engine.templating.pebbles.extensions.filters.*;
import com.davidrandoll.automation.engine.templating.spel.SpelTemplateEngine;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pebbletemplates.boot.autoconfigure.PebbleProperties;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.attributes.AttributeResolver;
import io.pebbletemplates.pebble.attributes.methodaccess.MethodAccessValidator;
import io.pebbletemplates.pebble.extension.*;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.pebble.operator.BinaryOperator;
import io.pebbletemplates.pebble.operator.UnaryOperator;
import io.pebbletemplates.pebble.tokenParser.TokenParser;
import io.pebbletemplates.spring.extension.SpringExtension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(AETemplatingProperties.class)
public class AETemplatingConfig {
    @Bean(name = "automationOptionsInterceptor")
    @Order(-100) // Run before other interceptors to ensure options are available
    @ConditionalOnMissingBean(name = "automationOptionsInterceptor", ignored = AutomationOptionsInterceptor.class)
    public AutomationOptionsInterceptor automationOptionsInterceptor() {
        return new AutomationOptionsInterceptor();
    }

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

    @Bean("jsonNodeVariableProcessor")
    @ConditionalOnMissingBean(name = "jsonNodeVariableProcessor", ignored = JsonNodeVariableProcessor.class)
    public JsonNodeVariableProcessor jsonNodeVariableProcessor(TemplateProcessor templateProcessor, ObjectMapper mapper, AETemplatingProperties properties) {
        return new JsonNodeVariableProcessor(templateProcessor, mapper, properties);
    }

    @Bean("pebble")
    @ConditionalOnMissingBean(name = "pebble", ignored = PebbleTemplateEngine.class)
    public PebbleTemplateEngine pebbleTemplateEngine(PebbleEngine pebbleEngine) {
        return new PebbleTemplateEngine(pebbleEngine);
    }

    @Bean("spel")
    @ConditionalOnMissingBean(name = "spel", ignored = SpelTemplateEngine.class)
    public SpelTemplateEngine spelTemplateEngine() {
        return new SpelTemplateEngine();
    }

    @Bean(name = "templateProcessor")
    @ConditionalOnMissingBean(name = "templateProcessor", ignored = TemplateProcessor.class)
    public TemplateProcessor templateProcessor(Map<String, ITemplateEngine> engines, AETemplatingProperties properties, ObjectMapper mapper) {
        return new TemplateProcessor(engines, properties.getDefaultEngine(), mapper);
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

    @Bean("date_format")
    @ConditionalOnMissingBean(name = "date_format", ignored = DateFormatFilter.class)
    public Filter dateFormatFilter() {
        return new DateFormatFilter();
    }

    @Bean("json")
    @ConditionalOnMissingBean(name = "json", ignored = JsonFilter.class)
    public Filter jsonFilter(ObjectMapper objectMapper) {
        return new JsonFilter(objectMapper);
    }

    @Bean("fromJson")
    @ConditionalOnMissingBean(name = "fromJson", ignored = FromJsonFilter.class)
    public Filter fromJsonFilter(ObjectMapper objectMapper) {
        return new FromJsonFilter(objectMapper);
    }

    @Bean("coalesce")
    @ConditionalOnMissingBean(name = "coalesce", ignored = CoalesceFilter.class)
    public Filter coalesceFilter() {
        return new CoalesceFilter();
    }

    @Bean("base64encode")
    @ConditionalOnMissingBean(name = "base64encode", ignored = Base64EncodeFilter.class)
    public Filter base64EncodeFilter() {
        return new Base64EncodeFilter();
    }

    @Bean("base64decode")
    @ConditionalOnMissingBean(name = "base64decode", ignored = Base64DecodeFilter.class)
    public Filter base64DecodeFilter() {
        return new Base64DecodeFilter();
    }

    @Bean("urlEncode")
    @ConditionalOnMissingBean(name = "urlEncode", ignored = UrlEncodeFilter.class)
    public Filter urlEncodeFilter() {
        return new UrlEncodeFilter();
    }

    @Bean("urlDecode")
    @ConditionalOnMissingBean(name = "urlDecode", ignored = UrlDecodeFilter.class)
    public Filter urlDecodeFilter() {
        return new UrlDecodeFilter();
    }

    @Bean("aEPebbleExtension")
    @ConditionalOnMissingBean(name = "aEPebbleExtension", ignored = AEPebbleExtension.class)
    public AbstractExtension customExtension(List<TokenParser> tokenParsers,
                                             List<BinaryOperator> binaryOperators,
                                             List<UnaryOperator> unaryOperators,
                                             Map<String, Filter> filters,
                                             Map<String, Test> tests,
                                             Map<String, Function> functions,
                                             List<NodeVisitorFactory> nodeVisitorFactories,
                                             List<AttributeResolver> attributeResolvers) {
        return new AEPebbleExtension(
                tokenParsers,
                binaryOperators,
                unaryOperators,
                filters,
                tests,
                functions,
                nodeVisitorFactories,
                attributeResolvers);
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
