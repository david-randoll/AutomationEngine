package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonSchemaConfig {
    @Bean
    public SchemaGenerator jsonSchemaGenerator() {
        JakartaValidationModule jakartaValidationModule = new JakartaValidationModule();
        JacksonModule jacksonModule = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_ORDER);
        JavadocDescriptionModule javadocDescriptionModule = new JavadocDescriptionModule();
        PresentationHintModule contextFieldModule = new PresentationHintModule();
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        configBuilder.with(jakartaValidationModule);
        configBuilder.with(jacksonModule);
        configBuilder.with(javadocDescriptionModule);
        configBuilder.with(contextFieldModule);
        configBuilder.with(new JsonAnySetterAsAdditionalPropsModule());
        configBuilder.with(new BlockTypeDefinitionModule());
        configBuilder.with(new NestedClassPrefixModule());
        configBuilder.with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
        SchemaGeneratorConfig config = configBuilder.build();
        return new SchemaGenerator(config);
    }
}