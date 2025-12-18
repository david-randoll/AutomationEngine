package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;

/**
 * Module to prefix nested class definitions with their parent class name.
 * This prevents naming collisions when multiple parent classes have nested
 * classes with the same name.
 * <p>
 * For example:
 * - IfThenElseActionContext.IfThenBlock becomes
 * "IfThenElseActionContext.IfThenBlock" instead of just "IfThenBlock"
 */
public class NestedClassPrefixModule implements Module {
    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        builder.forTypesInGeneral()
                .withDefinitionNamingStrategy((definitionKey, context) -> {
                    Class<?> clazz = definitionKey.getType().getErasedType();

                    // Check if this is a nested class (static member class or inner class)
                    if (clazz.getEnclosingClass() != null) {
                        return buildNestedClassName(clazz);
                    }

                    // Not a nested class, use default behavior (return simple class name)
                    return clazz.getSimpleName();
                });
    }

    /**
     * Build the custom name for a nested class by prefixing it with parent class
     * names.
     * Handles multiple levels of nesting.
     *
     * @param clazz the nested class
     * @return the custom name with parent class prefix(es)
     */
    private String buildNestedClassName(Class<?> clazz) {
        StringBuilder nameBuilder = new StringBuilder();
        buildNestedClassNameRecursive(clazz, nameBuilder);
        return nameBuilder.toString();
    }

    /**
     * Recursively build the nested class name from outermost to innermost class.
     *
     * @param clazz       the current class
     * @param nameBuilder the string builder to accumulate the name
     */
    private void buildNestedClassNameRecursive(Class<?> clazz, StringBuilder nameBuilder) {
        Class<?> enclosingClass = clazz.getEnclosingClass();

        if (enclosingClass != null) {
            // If the enclosing class is also nested, process it first
            if (enclosingClass.getEnclosingClass() != null) {
                buildNestedClassNameRecursive(enclosingClass, nameBuilder);
                nameBuilder.append(".");
            } else {
                // This is the outermost class
                nameBuilder.append(enclosingClass.getSimpleName());
                nameBuilder.append(".");
            }
        }

        // Append the current class name
        nameBuilder.append(clazz.getSimpleName());
    }
}
