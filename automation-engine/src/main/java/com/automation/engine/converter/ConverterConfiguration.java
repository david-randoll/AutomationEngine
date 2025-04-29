package com.automation.engine.converter;

import com.automation.engine.creator.parsers.yaml.IYamlConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public IYamlConverter yamlConverter() {
        return new YamlConverter();
    }
}