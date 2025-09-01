package com.davidrandoll.automation.engine.config;

import com.davidrandoll.automation.engine.converter.JsonConverter;
import com.davidrandoll.automation.engine.converter.TypeConverter;
import com.davidrandoll.automation.engine.converter.YamlConverter;
import com.davidrandoll.automation.engine.creator.parsers.json.IJsonConverter;
import com.davidrandoll.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.davidrandoll.automation.engine.spi.ITypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class ConverterConfig {
    @Bean
    @ConditionalOnMissingBean(value = IJsonConverter.class, ignored = JsonConverter.class)
    public JsonConverter jsonConverter(ObjectMapper mapper) {
        return new JsonConverter(mapper);
    }

    @Bean
    @ConditionalOnMissingBean(value = ITypeConverter.class, ignored = TypeConverter.class)
    public TypeConverter typeConverter(ObjectMapper mapper) {
        return new TypeConverter(mapper);
    }

    @Bean
    @ConditionalOnMissingBean(value = IYamlConverter.class, ignored = YamlConverter.class)
    @ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
    public YamlConverter yamlConverter() {
        return new YamlConverter();
    }
}
