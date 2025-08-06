package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonSchemaConfig {
    @Bean
    public JsonSchemaGenerator jsonSchemaGenerator(ObjectMapper mapper) {
        return new JsonSchemaGenerator(mapper);
    }
}