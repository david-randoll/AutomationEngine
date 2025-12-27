package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;

/**
 * Custom Victools module to extract field descriptions from @SchemaDescription annotation.
 * This module also falls back to extracting Javadoc comments at compile time if available.
 */
class JavadocDescriptionModule implements Module {

    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        builder.forFields()
                .withInstanceAttributeOverride((attributes, field, context) -> {
                    // Check for @SchemaDescription annotation
                    SchemaDescription annotation = field.getAnnotation(SchemaDescription.class);
                    if (attributes.get("x-presentation-help") != null) return;
                    if (annotation != null) {
                        attributes.put("x-presentation-help", annotation.value());
                    }
                });
    }
}