package com.automation.engine.config;

import com.automation.engine.converter.JsonConverter;
import com.automation.engine.converter.TypeConverter;
import com.automation.engine.converter.YamlConverter;
import com.automation.engine.creator.parsers.json.IJsonConverter;
import com.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.automation.engine.spi.ITypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {

    @Bean
    @ConditionalOnMissingBean
    public IJsonConverter jsonConverter(ObjectMapper objectMapper) {
        return new JsonConverter(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ITypeConverter typeConverter(ObjectMapper objectMapper) {
        return new TypeConverter(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public IYamlConverter yamlConverter() {
        return new YamlConverter();
    }
}
