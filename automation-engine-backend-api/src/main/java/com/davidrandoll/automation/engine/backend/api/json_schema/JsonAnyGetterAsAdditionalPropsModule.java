package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;

import java.util.Arrays;

public class JsonAnyGetterAsAdditionalPropsModule implements Module {
    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        builder.forTypesInGeneral()
                .withAdditionalPropertiesResolver((scope, context) -> {
                    Class<?> clazz = scope.getType().getErasedType();
                    boolean hasJsonAnyGetter = Arrays.stream(clazz.getDeclaredMethods())
                            .anyMatch(m -> m.isAnnotationPresent(JsonAnySetter.class));

                    if (hasJsonAnyGetter) {
                        // Use default object schema (any additional property allowed)
                        var type = context.getTypeContext().resolve(Object.class);
                        return context.createDefinitionReference(type);
                    }
                    return null;
                });
    }
}
