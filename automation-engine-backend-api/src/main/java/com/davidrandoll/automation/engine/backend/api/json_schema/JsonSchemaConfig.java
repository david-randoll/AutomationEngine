package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonSchemaConfig {
    @Bean
    public SchemaGenerator jsonSchemaGenerator() {
        JakartaValidationModule jakartaValidationModule = new JakartaValidationModule();
        JacksonModule jacksonModule = new JacksonModule();
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        configBuilder.with(jakartaValidationModule);
        configBuilder.with(jacksonModule);
        SchemaGeneratorConfig config = configBuilder.build();
        return new SchemaGenerator(config);
    }
}