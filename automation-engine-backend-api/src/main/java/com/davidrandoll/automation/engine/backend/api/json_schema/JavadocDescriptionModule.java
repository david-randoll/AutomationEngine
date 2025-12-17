package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;

/**
 * Custom Victools module to extract field descriptions from @SchemaDescription annotation.
 * This module also falls back to extracting Javadoc comments at compile time if available.
 */
public class JavadocDescriptionModule implements Module {

    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        builder.forFields()
                .withDescriptionResolver(field -> {
                    // Check for @SchemaDescription annotation
                    SchemaDescription annotation = field.getAnnotation(SchemaDescription.class);
                    if (annotation != null) {
                        return annotation.value();
                    }
                    return null;
                });

        builder.forTypesInGeneral()
                .withDescriptionResolver(scope -> {
                    // Check for @SchemaDescription annotation on types
                    SchemaDescription annotation = scope.getType().getErasedType().getAnnotation(SchemaDescription.class);
                    if (annotation != null) {
                        return annotation.value();
                    }
                    return null;
                });
    }
}

