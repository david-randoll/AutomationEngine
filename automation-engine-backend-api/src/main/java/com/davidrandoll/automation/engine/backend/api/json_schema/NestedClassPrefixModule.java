package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;

/**
 * Module to use fully qualified class names for all type definitions in JSON schemas.
 * This prevents naming collisions and provides complete context for all classes.
 * <p>
 * For example:
 * - Regular class: "MyClass" becomes "com.example.MyClass"
 * - Nested class: "IfThenBlock" becomes "com.example.IfThenElseActionContext.IfThenBlock"
 */
public class NestedClassPrefixModule implements Module {
    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        builder.forTypesInGeneral()
                .withDefinitionNamingStrategy((definitionKey, context) -> {
                    Class<?> clazz = definitionKey.getType().getErasedType();
                    return buildFullyQualifiedName(clazz);
                });
    }

    /**
     * Build the fully qualified name for any class.
     * For nested classes, uses '$' separator as per Java naming convention.
     * For regular classes, uses the package name.
     *
     * @param clazz the class
     * @return the fully qualified name (e.g., "com.example.Outer.Inner")
     */
    private String buildFullyQualifiedName(Class<?> clazz) {
        // Use the canonical name which handles nested classes properly with '.' separator
        // For nested classes: com.example.Outer.Inner
        // For regular classes: com.example.MyClass
        String canonicalName = clazz.getCanonicalName();
        
        // If canonical name is available (it might be null for local/anonymous classes), use it
        if (canonicalName != null) {
            return canonicalName;
        }
        
        // Fallback to getName() which uses '$' for nested classes
        // Then replace '$' with '.' for consistency
        return clazz.getName().replace('$', '.');
    }
}
