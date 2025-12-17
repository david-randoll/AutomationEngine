package com.davidrandoll.automation.engine.backend.api;

import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaService;
import com.davidrandoll.automation.engine.backend.api.services.AESchemaService;
import com.davidrandoll.automation.engine.backend.api.services.IAESchemaService;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AutomationEngineBackendApiProperties.class)
public class AEBackendApiConfig {
    @Bean
    @ConditionalOnMissingBean
    public IAESchemaService automationEngineSchemaService(JsonSchemaService jsonSchemaService,
            ApplicationContext application) {
        return new AESchemaService(jsonSchemaService, application);
    }

    @Bean
    public JsonSchemaService jsonSchemaService(SchemaGenerator jsonSchemaGenerator) {
        return new JsonSchemaService(jsonSchemaGenerator);
    }
}